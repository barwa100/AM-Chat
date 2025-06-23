using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Linq;
using System.Security.Cryptography;
using System.Text;
using System.Threading.Tasks;
using AM_Chat_WebServer.Data;
using AM_Chat_WebServer.Data.Models;
using AM_Chat_WebServer.Data.Requests;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;

namespace AM_Chat_WebServer;

[Authorize]
public class ChatHub(ChatDbContext dbContext, MediaService mediaService) : Hub<IChatClient>
{
    public static readonly ConcurrentDictionary<long, HashSet<string>> UserConnections = new();

    public override async Task OnConnectedAsync()
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var channels = await dbContext.Channels
            .Include(c => c.Members)
            .Where(c => c.Members.Any(u => u.Id == id))
            .ToListAsync();
        foreach (var channel in channels)
        {
            await Groups.AddToGroupAsync(Context.ConnectionId, channel.Id.ToString());
        }
        var connections = UserConnections.GetOrAdd(id, _ => new HashSet<string>());
        lock (connections)
        {
            connections.Add(Context.ConnectionId);
        }
        await base.OnConnectedAsync();
    }

    public override Task OnDisconnectedAsync(Exception? exception)
    {
        var userId =
            long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        if (userId != null && UserConnections.TryGetValue(userId, out var connections))
        {
            lock (connections)
            {
                connections.Remove(Context.ConnectionId);
                if (connections.Count == 0)
                {
                    UserConnections.TryRemove(userId, out _);
                }
            }
        }

        return base.OnDisconnectedAsync(exception);
    }

    public async Task SendMessage(SendTextMessage msg)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users.FindAsync(id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }
        
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == msg.ChannelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }

        var message = new Message
        {
            ChannelId = msg.ChannelId,
            Type = MessageType.Text,
            Data = msg.Text,
            Created = DateTimeOffset.UtcNow,
            SenderId = id
        };
        
        dbContext.Messages.Add(message);
        await dbContext.SaveChangesAsync();
        
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(message.ToDto());
    }

    public async Task SendMediaMessage(SendMediaMessage msg)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users.FindAsync(id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }   
        
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == msg.ChannelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }


        var message = await mediaService.SaveMedia(msg, id);
        
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(message.ToDto());
    }

    public async Task<List<Channel>> GetChannels()
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));

        var channels = await dbContext.Channels
            .Include(c => c.Members)
            .Include(c => c.Messages)
            .Where(c => c.Members.Any(u => u.Id == id))
            .ToListAsync();
        await Clients.Caller.GetChannels(channels.Select(c => c.ToDto()).ToList());
        return channels;
    }
    
    public async Task<Channel> CreateChannel(CreateChannelRequest request)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        request.UserIds ??= new List<long>();
        request.UserIds.Add(id);
        var users = await dbContext.Users
            .Where(u => request.UserIds.Contains(u.Id))
            .ToListAsync();
        var channel = new Channel
        {
            Name = request.Name,
            Members = users,
            Created = DateTimeOffset.UtcNow
        };
        dbContext.Channels.Add(channel);
        await dbContext.SaveChangesAsync();
        foreach (var user in users)
        {
            if (UserConnections.TryGetValue(user.Id, out var connections))
            {
                foreach (var connection in connections)
                {
                    await Clients.Client(connection).ChannelCreated(channel.ToDto());
                    await Groups.AddToGroupAsync(connection, channel.Id.ToString());
                }
            }
        }
        return channel;
    }
    
    public async Task AddToChannel(long channelId, long userId)
    {
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == channelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        var user = await dbContext.Users.FindAsync(userId);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }
        
        channel.Members.Add(user);
        await dbContext.SaveChangesAsync();
        
        if (UserConnections.TryGetValue(user.Id, out var connections))
        {
            foreach (var connection in connections)
            {
                await Clients.Client(connection).ChannelCreated(channel.ToDto());
                await Groups.AddToGroupAsync(connection, channel.Id.ToString());
            }
        }
        var adder = await dbContext.Users.FindAsync(long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null")));
        var msg = new Message()
        {
            ChannelId = channel.Id,
            Type = MessageType.Text,
            Data = $"{adder.UserName} dodał {user.UserName} do kanału",
            Created = DateTimeOffset.UtcNow
        };        
        dbContext.Messages.Add(msg);
        await dbContext.SaveChangesAsync();
        Clients.Group(channel.Id.ToString()).UserJoined(channel.ToDto(), user.ToDto(), adder.ToDto());
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(msg.ToDto());
    }
    
    public async Task<List<User>> GetContacts()
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users
            .Include(u => u.Contacts)
            .ThenInclude(x=> x.Other)
            .FirstOrDefaultAsync(u => u.Id == id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }
        await Clients.Caller.GetContacts(user.Contacts.Select(c => c.Other.ToDto()).ToList());
        return user.Contacts.Select(x=>x.Other).ToList();
    }
    
    public async Task AddContact(string userName)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users.Include(x=>x.Contacts).FirstOrDefaultAsync(x => x.Id == id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }
        
        var contact = await dbContext.Users.FirstOrDefaultAsync(x => x.UserName == userName);
        if (contact == null)
        {
            throw new InvalidOperationException("Contact not found");
        }
        
        if (user.Contacts.Any(c => c.OtherId == contact.Id))
        {
            throw new InvalidOperationException("Contact already exists");
        }
        
        user.Contacts.Add(new Contact
        {
            OtherId = contact.Id,
            UserId = user.Id
        });
        await dbContext.SaveChangesAsync();
        if (UserConnections.TryGetValue(contact.Id, out var connections))
        {
            foreach (var connection in connections)
            {
                await Clients.Client(connection).NewContact(contact.ToDto());
            }
        }
        await Clients.Caller.NewContact(contact.ToDto());
    }
    
    public async Task<List<Message>> GetChannelMessages(long channelId, long? beforeMessageId = null, int limit = 50)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == channelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }

        var query = dbContext.Messages
            .Where(m => m.ChannelId == channelId)
            .ToList()
            .OrderByDescending(m => m.Created)
            .AsQueryable();

        if (beforeMessageId.HasValue)
        {
            query = query.Where(m => m.Id < beforeMessageId.Value);
        }

        var messages = query.Take(limit).ToList();
        
        await Clients.Caller.GetChannelMessages(messages.Select(x=> x.ToDto()).ToList());
        return messages;
    }
    
    public async Task<List<User>> GetChannelMembers(long channelId)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == channelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }

        await Clients.Caller.GetChannelMembers(channel.Members.Select(m => m.ToDto()).ToList());
        return channel.Members;
    }
    
    public async Task<Channel> GetChannel(long channelId)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .Include(m => m.Messages)
            .FirstOrDefaultAsync(c => c.Id == channelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }

        await Clients.Caller.GetChannel(channel.ToDto());
        return channel;
    }
    
    public async Task<User> GetCurrentUser()
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users
            .Include(u => u.Contacts)
            .FirstOrDefaultAsync(u => u.Id == id);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }
        await Clients.Caller.GetCurrentUser(user.ToDto());
        return user;
    }
    
    public async Task DeleteChannel(long channelId)
    {
        var userId = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));

        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == channelId);

        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }
        
        if (!channel.Members.Any(u => u.Id == userId))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }
        var messages = await dbContext.Messages
            .Where(m => m.ChannelId == channelId)
            .ToListAsync();

        dbContext.Messages.RemoveRange(messages);
        dbContext.Channels.Remove(channel);
        await dbContext.SaveChangesAsync();
        
        await Clients.Group(channelId.ToString()).ChannelDeleted(channelId);
    }

    public async Task RenameChannel(long channelId, string newName)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var channel = await dbContext.Channels
            .Include(c => c.Members)
            .FirstOrDefaultAsync(c => c.Id == channelId);
        if (channel == null)
        {
            throw new InvalidOperationException("Channel not found");
        }

        if (!channel.Members.Any(u => u.Id == id))
        {
            throw new InvalidOperationException("You are not a member of this channel");
        }

        var oldName = channel.Name;

        channel.Name = newName;
        await dbContext.SaveChangesAsync();

        var user = await dbContext.Users.FindAsync(id);
        var message = new Message
        {
            ChannelId = channelId,
            Type = MessageType.Text,
            Data = $"{user.UserName} zmienił nazwę kanału z \"{oldName}\" na \"{newName}\"",
            Created = DateTimeOffset.UtcNow,
            SenderId = id
        };

        dbContext.Messages.Add(message);
        await dbContext.SaveChangesAsync();

        await Clients.Group(channelId.ToString()).ChannelNameChanged(channelId, newName);
        await Clients.Group(channelId.ToString()).ReceiveMessage(message.ToDto());

        await Clients.Group(channelId.ToString()).GetChannel(channel.ToDto());
    }

    public async Task ChangeUserAvatar(string avatarDataBase64, string extension)
    {
        var userId = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        var user = await dbContext.Users.FindAsync(userId);
        if (user == null)
        {
            throw new InvalidOperationException("User not found");
        }

        try
        {
            // Konwersja base64 na tablicę bajtów
            byte[] avatarData = Convert.FromBase64String(avatarDataBase64);

            // Zapisanie pliku awatara
            if (!Directory.Exists(MediaService.Path))
                Directory.CreateDirectory(MediaService.Path);

            var fileName = $"avatar_{user.Id}.{extension}";
            var filePath = Path.Combine(MediaService.Path, fileName);

            await File.WriteAllBytesAsync(filePath, avatarData);

            // Aktualizacja ścieżki do awatara w bazie danych
            user.AvatarUrl = $"Media/{fileName}";
            await dbContext.SaveChangesAsync();

            // Powiadomienie wszystkich użytkowników o zmianie awatara
            await Clients.All.UserAvatarChanged(user.Id, user.AvatarUrl);

            // Wysłanie zaktualizowanego użytkownika do osoby zmieniającej awatar
            await Clients.Caller.GetCurrentUser(user.ToDto());
        }
        catch (Exception ex)
        {
            throw new InvalidOperationException($"Failed to update avatar: {ex.Message}");
        }
    }
}
