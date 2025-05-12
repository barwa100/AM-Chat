using AM_Chat_WebServer.Data;
using Microsoft.AspNetCore.SignalR;

namespace AM_Chat_WebServer;

public class Program
{
    public static void Main(string[] args)
    {
        new SnowflakeGenerator();
        var builder = WebApplication.CreateBuilder(args);

        // Add services to the container.
        builder.Services.AddAuthorization();

        // Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
        builder.Services.AddOpenApi();
        builder.Services.AddSignalR();
        builder.Services.AddDbContextFactory<ChatDbContext>();
        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.MapOpenApi();
        }

        app.UseHttpsRedirection();

        app.UseAuthorization();
        app.MapHub<ChatHub>("/ws");
        var summaries = new[]
        {
            "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
        };
        app.MapGet("/send/{message}", (HttpContext context, IHubContext<ChatHub, IChatClient> chatHub, string message) =>
        {
            chatHub.Clients.All.ReceiveMessage(message);
        });

        app.Run();
    }
}