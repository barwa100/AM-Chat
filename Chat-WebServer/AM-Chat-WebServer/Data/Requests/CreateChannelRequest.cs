namespace AM_Chat_WebServer.Data.Requests;

public class CreateChannelRequest
{
    public List<long> UserIds { get; set; }
    public string Name { get; set; }
}