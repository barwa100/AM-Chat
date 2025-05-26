namespace AM_Chat_WebServer.Data.Models;

public class Identifiable
{
    public long Id { get; set; } = SnowflakeGenerator.Instance.Generator.NewSnowflake();
}