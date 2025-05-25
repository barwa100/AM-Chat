using System.Collections.Concurrent;
using System.Security.Cryptography;
using System.Text;
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
    private static readonly ConcurrentDictionary<long, HashSet<string>> UserConnections = new();

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
        
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(message);
    }

    public async Task SentMediaMessage(SendMediaMessage msg)
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

        var message = await mediaService.SaveMedia(msg);
        message.SenderId = id;
        message.ChannelId = msg.ChannelId;
        message.Created = DateTimeOffset.UtcNow;
        
        dbContext.Messages.Add(message);
        await dbContext.SaveChangesAsync();
        
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(message);
        
    }

    public async Task<List<Channel>> GetChannels()
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));

        var channels = await dbContext.Channels
            .Include(c => c.Members)
            .Where(c => c.Members.Any(u => u.Id == id))
            .ToListAsync();
        await Clients.Caller.GetChannels(channels);
        return channels;
    }
    
    public async Task<Channel> CreateChannel(CreateChannelRequest request)
    {
        var id = long.Parse(Context.UserIdentifier ?? throw new InvalidOperationException("UserIdentifier is null"));
        request.UserIds.Add(id);
        var users = await dbContext.Users
            .Where(u => request.UserIds.Contains(u.Id))
            .ToListAsync();
        var channel = new Channel
        {
            Name = request.Name,
            Members = users
        };
        dbContext.Channels.Add(channel);
        await dbContext.SaveChangesAsync();
        foreach (var user in users)
        {
            if (UserConnections.TryGetValue(user.Id, out var connections))
            {
                foreach (var connection in connections)
                {
                    await Clients.Client(connection).ChannelCreated(channel);
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
                await Clients.Client(connection).ChannelCreated(channel);
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
        Clients.Group(channel.Id.ToString()).UserJoined(channel, user, adder);
        await Clients.Group(channel.Id.ToString()).ReceiveMessage(msg);
    }
}