using AM_Chat_WebServer.Data.DTOs;

namespace AM_Chat_WebServer.Data.Models;

public class Channel
{
    public long Id { get; set; } = SnowflakeGenerator.Instance.Generator.NewSnowflake();
    public List<User> Members { get; set; } = new();
    public List<Message> Messages { get; set; } = new();
    public string Name { get; set; }
    public DateTimeOffset Created { get; set; }
    
    public ChannelDTO ToDto()
    {
        return new ChannelDTO
        {
            Id = Id,
            Members = Members.Select(m => m.Id).ToList(),
            Messages = Messages.Select(m => m.Id).ToList(),
            Name = Name,
            LastMessage = Messages.OrderByDescending(m => m.Created).FirstOrDefault()?.ToDto(),
            Created = Created.ToUnixTimeMilliseconds()
        };
    }
}