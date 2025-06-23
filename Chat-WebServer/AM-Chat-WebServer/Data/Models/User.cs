using System.ComponentModel.DataAnnotations;
using AM_Chat_WebServer.Data.DTOs;
using Microsoft.AspNetCore.Identity;

namespace AM_Chat_WebServer.Data.Models;

public class User
{
    [Key]
    public long Id { get; set; } = SnowflakeGenerator.Instance.Generator.NewSnowflake();
    public string UserName { get; set; }
    public string Password { get; set; } = string.Empty;
    public List<Channel> Channels { get; set; } = new();
    public List<Message> Messages { get; set; } = new();
    public string AvatarUrl { get; set; } = string.Empty;
    
    public List<Contact> Contacts { get; set; } = new List<Contact>();

    public UserDTO ToDto()
    {
        return new UserDTO
        {
            Id = Id,
            UserName = UserName,
            AvatarUrl = AvatarUrl,
            Contacts = Contacts.Select(c => c.OtherId).ToList(),
            Channels = Channels.Select(c => c.Id).ToList(),
            Messages = Messages.Select(m => m.Id).ToList()
        };
    }
}