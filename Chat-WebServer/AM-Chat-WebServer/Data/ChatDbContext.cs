using AM_Chat_WebServer.Data.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.Identity.EntityFrameworkCore;
using Microsoft.EntityFrameworkCore;

namespace AM_Chat_WebServer.Data;

public class ChatDbContext : DbContext
{
    public ChatDbContext(DbContextOptions<ChatDbContext> options) : base(options)
    {
    }
    public DbSet<User> Users { get; set; }
    public DbSet<Message> Messages { get; set; }
    public DbSet<Channel> Channels { get; set; }

    protected override void OnModelCreating(ModelBuilder modelBuilder)
    {
        // modelBuilder.Entity<Identifiable>()
        //     .Property(x => x.Id)
        //     .HasValueGenerator((property, @base) => SnowflakeGenerator.Instance)
        //     .ValueGeneratedOnAdd();
        //
        // modelBuilder.Entity<User>()
        //     .Property(x => x.Id)
        //     .HasValueGenerator((property, @base) => SnowflakeGenerator.Instance)
        //     .ValueGeneratedOnAdd();
        base.OnModelCreating(modelBuilder);
    }
}