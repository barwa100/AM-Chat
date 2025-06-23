using AM_Chat_WebServer.Data.DTOs;

namespace AM_Chat_WebServer.Data.Models;

public class Message
{
    public long Id { get; set; } = SnowflakeGenerator.Instance.Generator.NewSnowflake();
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
            MessageType = Type,
            Created = Created.ToUnixTimeMilliseconds(),
            Updated = Updated.HasValue ? Updated.Value.ToUnixTimeMilliseconds() : null
        };
    }
}

public enum MessageType
{
    Text = 1,
    Image = 2,
    Video = 3,
    Audio = 4
}

