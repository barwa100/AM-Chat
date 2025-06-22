using System;
using System.IO;
using System.Threading.Tasks;
using AM_Chat_WebServer.Data;
using AM_Chat_WebServer.Data.Models;
using AM_Chat_WebServer.Data.Requests;

namespace AM_Chat_WebServer;

public class MediaService(ChatDbContext dbContext)
{
    public static string Path = System.IO.Path.Combine(AppDomain.CurrentDomain.BaseDirectory, "Media");
    public async Task<Message> SaveMedia(SendMediaMessage msg)
    {
        var message = new Message
        {
            ChannelId = msg.ChannelId,
            Type = msg.MessageType,
            Data = msg.Extension,
            Created = DateTimeOffset.UtcNow
        };
        dbContext.Add(message);
        await dbContext.SaveChangesAsync();
        var file = File.Create(System.IO.Path.Combine(Path, message.Id + "." + message.Data));
        await file.WriteAsync(msg.Data);
        file.Close();
        return message;
    }

    public FileStream? GetMedia(string message)
    {
        if (System.IO.File.Exists(System.IO.Path.Combine(Path, message)))
            return File.Open(System.IO.Path.Combine(Path, message), FileMode.Open, FileAccess.Read);
        return null;
    }
}