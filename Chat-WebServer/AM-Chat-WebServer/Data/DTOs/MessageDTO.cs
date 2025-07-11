﻿using AM_Chat_WebServer.Data.Models;

namespace AM_Chat_WebServer.Data.DTOs;

public class MessageDTO
{
    public long Id { get; set; }
    public long SenderId { get; set; }
    public long ChannelId { get; set; }
    public string Data { get; set; } = string.Empty;
    public MessageType MessageType { get; set; }
    public long Created { get; set; }
    public long? Updated { get; set; } = null;
}