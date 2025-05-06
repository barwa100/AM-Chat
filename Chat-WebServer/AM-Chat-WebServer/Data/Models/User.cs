namespace AM_Chat_WebServer.Data.Models;

public class User : IIdentifiable
{
    public ulong Id { get; set; }
    public string Username { get; set; }
    public string Password { get; set; }
    public List<Channel> Channels { get; set; }
    public List<Message> Messages { get; set; }
}