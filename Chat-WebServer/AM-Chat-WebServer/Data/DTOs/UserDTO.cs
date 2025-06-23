namespace AM_Chat_WebServer.Data.DTOs;

public class UserDTO
{
    public long Id { get; set; }
    public string UserName { get; set; }
    public string? AvatarUrl { get; set; }
    public List<long> Contacts { get; set; } = new List<long>();
    public List<long> Channels { get; set; } = new List<long>();
    public List<long> Messages { get; set; } = new List<long>();
}