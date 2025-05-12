using Microsoft.AspNetCore.SignalR;

namespace AM_Chat_WebServer;

public class ChatHub : Hub<IChatClient>
{
    public async Task SendMessage(string msg)
    {
        await Clients.All.ReceiveMessage(msg);
    }
    
}