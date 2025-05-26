using AM_Chat_WebServer.Data.DTOs;

namespace AM_Chat_WebServer.Data.Models;

public class Message : Identifiable
{
    public long Id { get; set; }
    public virtual User Sender { get; set; }
    public long SenderId { get; set; }
    public virtual Channel Channel { get; set; }
    public long ChannelId { get; set; }
    public string Data { get; set; }
    public MessageType Type { get; set; }
    public DateTimeOffset Created { get; set; }
    public DateTimeOffset? Updated { get; set; }

    public MessageDTO ToDto()
    {
        return new MessageDTO
        {
            Id = Id,
            SenderId = SenderId,
            ChannelId = ChannelId,
            Data = Data,
            Type = Type,
            Created = Created,
            Updated = Updated
        };
    }
}

public enum MessageType
{
    Text,
    Image,
    Video,
    Audio
}