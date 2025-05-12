namespace AM_Chat_WebServer.Data.Models;

public class Channel : IIdentifiable
{
    public ulong Id { get; set; }
    public List<User> Members { get; set; }
    public List<Message> Messages { get; set; }
    public string Name { get; set; }
}