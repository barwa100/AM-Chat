using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer.Data.Requests;

public class SendMediaMessage
{
    public long ChannelId { get; set; }
    public byte[] Data { get; set; }
    public string Extension { get; set; }
    public MessageType MessageType { get; set; }
}