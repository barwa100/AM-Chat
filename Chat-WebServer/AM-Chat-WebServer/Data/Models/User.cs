using Microsoft.AspNetCore.Identity;

namespace AM_Chat_WebServer.Data.Models;

public class User : IdentityUser<long>
{
    public User()
    {
        Channels = new List<Channel>();
        Messages = new List<Message>();
        Id = SnowflakeGenerator.Instance.Generator.NewSnowflake();
    }
    public List<Channel> Channels { get; set; }
    public List<Message> Messages { get; set; }
}