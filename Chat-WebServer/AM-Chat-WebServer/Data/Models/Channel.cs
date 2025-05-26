using AM_Chat_WebServer.Data.DTOs;

namespace AM_Chat_WebServer.Data.Models;

public class Channel : Identifiable
{
    public long Id { get; set; }
    public List<User> Members { get; set; }
    public List<Message> Messages { get; set; }
    public string Name { get; set; }
    
    public ChannelDTO ToDto()
    {
        return new ChannelDTO
        {
            Id = Id,
            Members = Members.Select(m => m.Id).ToList(),
            Messages = Messages.Select(m => m.Id).ToList(),
            Name = Name
        };
    }
}