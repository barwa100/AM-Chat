using AM_Chat_WebServer.Data.Models;
using Microsoft.EntityFrameworkCore;

namespace AM_Chat_WebServer.Data;

public class ChatDbContext : DbContext
{
    DbSet<User> Users { get; set; }
    DbSet<Message> Messages { get; set; }
    public DbSet<Channel> Channels { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        modelBuilder.Entity<IIdentifiable>()
            .Property(x => x.Id)
            .HasValueGenerator((property, @base) => SnowflakeGenerator.Instance)
            .ValueGeneratedOnAdd();
        base.OnModelCreating(modelBuilder);
    }
}