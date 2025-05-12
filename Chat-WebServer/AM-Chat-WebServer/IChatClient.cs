namespace AM_Chat_WebServer;

public interface IChatClient
{
    public Task ReceiveMessage(string message);
}