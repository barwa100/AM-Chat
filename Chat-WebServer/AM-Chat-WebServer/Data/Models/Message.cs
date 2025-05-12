namespace AM_Chat_WebServer.Data.Models;

public class Message : IIdentifiable
{
    public ulong Id { get; set; }
    public User Sender { get; set; }
    public Channel Channel { get; set; }
    public string Data { get; set; }
    public MessageType Type { get; set; }
    public DateTimeOffset Created { get; set; }
    public DateTimeOffset? Updated { get; set; }
}

public enum MessageType
{
    Text,
    Image,
    Video,
    Audio
}