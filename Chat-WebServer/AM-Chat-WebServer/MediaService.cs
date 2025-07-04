﻿using System;
using System.IO;
using System.Threading.Tasks;
using AM_Chat_WebServer.Data;
using AM_Chat_WebServer.Data.Models;
using AM_Chat_WebServer.Data.Requests;

namespace AM_Chat_WebServer;

public class MediaService(ChatDbContext dbContext)
{
    public static string Path = System.IO.Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Media");
    public async Task<Message> SaveMedia(SendMediaMessage msg, long userId)
    {
        var message = new Message
        {
            ChannelId = msg.ChannelId,
            SenderId = userId,
            Type = msg.MessageType,
            Data = msg.Extension,
            Created = DateTimeOffset.UtcNow
        };
        dbContext.Add(message);
        await dbContext.SaveChangesAsync();
        
        byte[] mediaData = Convert.FromBase64String(msg.DataBase64);
        if (!Directory.Exists(Path))
            Directory.CreateDirectory(Path);
        var file = File.Create(System.IO.Path.Combine(Path, message.Id + "." + message.Data));
        await file.WriteAsync(mediaData);
        file.Close();
        return message;
    }
    public async Task<User?> ChangeAvatar(IFormFile file, long userId)
    {
        if (file == null || file.Length == 0)
            return null;

        var user = await dbContext.Users.FindAsync(userId);
        if (user == null)
            return null;

        if (!Directory.Exists(Path))
            Directory.CreateDirectory(Path);
        var filePath = System.IO.Path.Combine(Path, user.Id + System.IO.Path.GetExtension(file.FileName).ToLowerInvariant());
        using (var stream = new FileStream(filePath, FileMode.Create))
        {
            await file.CopyToAsync(stream);
        }

        user.AvatarUrl = "media/" + user.Id + System.IO.Path.GetExtension(file.FileName).ToLowerInvariant();
        dbContext.Update(user);
        await dbContext.SaveChangesAsync();
        
        return user;
    }

    public FileStream? GetMedia(string message)
    {
        if (System.IO.File.Exists(System.IO.Path.Combine(Path, message)))
            return File.Open(System.IO.Path.Combine(Path, message), FileMode.Open, FileAccess.Read);
        return null;
    }
}