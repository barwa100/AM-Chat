using AM_Chat_WebServer.Data;
using AM_Chat_WebServer.Data.Models;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using Scalar.AspNetCore;

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
        builder.Services.AddControllers();
        builder.Services.AddIdentityApiEndpoints<User>(x =>
            {
                x.SignIn.RequireConfirmedAccount = false;
                x.SignIn.RequireConfirmedEmail = false;
                x.SignIn.RequireConfirmedPhoneNumber = false;
                x.Password.RequiredLength = 6;
                x.Password.RequireDigit = false;
                x.Password.RequireLowercase = false;
                x.Password.RequireNonAlphanumeric = false;
                x.Password.RequireUppercase = false;
                x.User.RequireUniqueEmail = true;
            })
            .AddEntityFrameworkStores<ChatDbContext>();
        builder.Services.AddSignalR();
        builder.Services.AddScoped<MediaService>();
        builder.Services.AddDbContext<ChatDbContext>(x => x.UseInMemoryDatabase("ChatDb"));
        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (app.Environment.IsDevelopment())
        {
            app.MapOpenApi();
            app.MapScalarApiReference();
        }

        app.UseHttpsRedirection();

        app.UseAuthorization();
        app.MapControllers();
        app.MapIdentityApi<User>();
        app.MapHub<ChatHub>("/ws");
        var summaries = new[]
        {
            "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
        };
        app.MapGet("/send/{message}", (HttpContext context, IHubContext<ChatHub, IChatClient> chatHub, string message) =>
        {
            //chatHub.Clients.All.ReceiveMessage(message);
        });
        app.Run();
    }
}