using AM_Chat_WebServer.Data.DTOs;
using Microsoft.AspNetCore.Identity;

namespace AM_Chat_WebServer.Data.Models;

public class User : Identifiable
{
    public string UserName { get; set; }
    public string Password { get; set; } = string.Empty;
    public List<Channel> Channels { get; set; } = new();
    public List<Message> Messages { get; set; } = new();
    public string AvatarUrl { get; set; } = string.Empty;
    
    public List<User> Contacts { get; set; } = new List<User>();

    public UserDTO ToDto()
    {
        return new UserDTO
        {
            Id = Id,
            UserName = UserName,
            AvatarUrl = AvatarUrl,
            Contacts = Contacts.Select(c => c.Id).ToList(),
            Channels = Channels.Select(c => c.Id).ToList(),
            Messages = Messages.Select(m => m.Id).ToList()
        };
    }
}