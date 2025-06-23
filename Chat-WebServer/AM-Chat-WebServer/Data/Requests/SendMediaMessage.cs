using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer.Data.Requests;

public class SendMediaMessage
{
    public long ChannelId { get; set; }
    public string DataBase64 { get; set; }  // Zmienione z byte[] Data na string z danymi Base64
    public string Extension { get; set; }
    public MessageType MessageType { get; set; }
}