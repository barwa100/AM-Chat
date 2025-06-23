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
    public DbSet<Contact> Contacts { get; set; }

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

        // Konfiguracja relacji dla Contact.User
        modelBuilder.Entity<Contact>()
            .HasOne(c => c.User)
            .WithMany(u => u.Contacts)  // Dwukierunkowa relacja z User.Contacts
            .HasForeignKey(c => c.UserId)
            .OnDelete(DeleteBehavior.Restrict);

        // Konfiguracja relacji dla Contact.Other
        modelBuilder.Entity<Contact>()
            .HasOne(c => c.Other)
            .WithMany()  // Ta relacja nie ma odpowiednika w klasie User
            .HasForeignKey(c => c.OtherId)
            .OnDelete(DeleteBehavior.Restrict);

        base.OnModelCreating(modelBuilder);
    }
}