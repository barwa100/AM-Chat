using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer;

public interface IChatClient
{
    public Task ReceiveMessage(Message message);
    public Task GetChannels(List<Channel> channels);
    public Task ChannelCreated(Channel channel);
    public Task UserJoined(Channel channel, User user, User adder);
}