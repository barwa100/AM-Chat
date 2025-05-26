using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer.Data.DTOs;

public class MessageDTO
{
    public long Id { get; set; }
    public long SenderId { get; set; }
    public long ChannelId { get; set; }
    public string Data { get; set; } = string.Empty;
    public MessageType Type { get; set; }
    public DateTimeOffset Created { get; set; }
    public DateTimeOffset? Updated { get; set; } = null;
}