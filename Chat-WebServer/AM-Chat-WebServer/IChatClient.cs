using System.Collections.Generic;
using System.Threading.Tasks;
using AM_Chat_WebServer.Data.DTOs;
using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer;

public interface IChatClient
{
    public Task ReceiveMessage(MessageDTO message);
    public Task GetChannels(List<ChannelDTO> channels);
    public Task GetChannel(ChannelDTO channel);
    public Task GetChannelMessages(List<MessageDTO> messages);
    public Task GetChannelMembers(List<UserDTO> members);
    public Task ChannelCreated(ChannelDTO channel);
    public Task UserJoined(ChannelDTO channel, UserDTO user, UserDTO adder);
    public Task GetContacts(List<UserDTO> contacts);
    public Task NewContact(UserDTO contact);

    public Task GetCurrentUser(UserDTO user);
    public Task ChannelDeleted(long channelId);
    public Task ChannelNameChanged(long channelId, string newName);
    public Task UserAvatarChanged(long userId, string newAvatarUrl);
}
