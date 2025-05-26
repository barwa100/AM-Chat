namespace AM_Chat_WebServer.Data.Requests;

public class SendTextMessage
{
    public long ChannelId { get; set; }
    public string Text { get; set; }
}