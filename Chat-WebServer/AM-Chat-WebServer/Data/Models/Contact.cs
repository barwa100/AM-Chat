using System.ComponentModel.DataAnnotations;
using Microsoft.EntityFrameworkCore;

namespace AM_Chat_WebServer.Data.Models;

public class Contact
{
    public int Id { get; set; }
    public long UserId { get; set; }
    public virtual User User { get; set; }
    public long OtherId { get; set; }
    public virtual User Other { get; set; }
}