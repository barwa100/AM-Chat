namespace AM_Chat_WebServer.Data.DTOs;

public class ChannelDTO
{
    public long Id { get; set; }
    public List<long> Members { get; set; } = new List<long>();
    public List<long> Messages { get; set; } = new List<long>();
    public string Name { get; set; } = string.Empty;
}