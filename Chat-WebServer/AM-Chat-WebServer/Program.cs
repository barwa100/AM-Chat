using System.Collections.Generic;
using AM_Chat_WebServer.Data;
using AM_Chat_WebServer.Data.Models;
using Microsoft.AspNetCore.Authentication.BearerToken;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.Identity;
using Microsoft.AspNetCore.SignalR;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Hosting;
using Scalar.AspNetCore;
using Serilog;

namespace AM_Chat_WebServer;

public class Program
{
    public static void Main(string[] args)
    {
        Log.Logger = new LoggerConfiguration()
            .WriteTo.Console()
            .MinimumLevel.Verbose()
            .CreateLogger();

        new SnowflakeGenerator();
        var builder = WebApplication.CreateBuilder(args);

        // Add services to the container.
        builder.Services.AddAuthorization();
        builder.Services.AddSerilog();
        // Learn more about configuring OpenAPI at https://aka.ms/aspnet/openapi
        builder.Services.AddOpenApi();
        builder.Services.AddControllers();
        // builder.Services.AddIdentityApiEndpoints<User>(x =>
        //     {
        //         x.SignIn.RequireConfirmedAccount = false;
        //         x.SignIn.RequireConfirmedEmail = false;
        //         x.SignIn.RequireConfirmedPhoneNumber = false;
        //         x.Password.RequiredLength = 6;
        //         x.Password.RequireDigit = false;
        //         x.Password.RequireLowercase = false;
        //         x.Password.RequireNonAlphanumeric = false;
        //         x.Password.RequireUppercase = false;
        //         x.User.RequireUniqueEmail = true;
        //     })
        //     .AddEntityFrameworkStores<ChatDbContext>();
        builder.Services.AddAuthentication(BearerTokenDefaults.AuthenticationScheme)
            .AddBearerToken(BearerTokenDefaults.AuthenticationScheme);
        builder.Services.AddSignalR();
        builder.Services.AddScoped<MediaService>();
        builder.Services.AddDbContext<ChatDbContext>(x => x.UseSqlite("Data Source=chat.db"));
        var app = builder.Build();

        // Configure the HTTP request pipeline.
        if (true)
        {
            app.MapOpenApi();
            app.MapScalarApiReference();
        }

        app.Services.CreateScope().ServiceProvider.GetRequiredService<ChatDbContext>().Database.EnsureCreated();
        
        //app.UseHttpsRedirection();

        app.UseAuthorization();
        app.MapControllers();
        //app.MapIdentityApi<User>();
        app.MapHub<ChatHub>("/ws");
        var summaries = new[]
        {
            "Freezing", "Bracing", "Chilly", "Cool", "Mild", "Warm", "Balmy", "Hot", "Sweltering", "Scorching"
        };
        app.MapGet("/send/{channelId}/{message}", (HttpContext context, IHubContext<ChatHub, IChatClient> chatHub, long channelId, string message) =>
        {
            //chatHub.Clients.All.ReceiveMessage(message);
        });
        app.MapGet("/channel/{username}", async (HttpContext context, IHubContext<ChatHub, IChatClient> chatHub, ChatDbContext dbContext, string username) =>
        {
            var user = await dbContext.Users.FirstOrDefaultAsync(x => x.UserName == username);
            if (user == null)
            {
                return Results.Problem("User not found", statusCode: StatusCodes.Status404NotFound);
            }

            var channel = new Channel()
            {
                Name = "AM-Chat",
                Members = new List<User>() { user }
            };
            dbContext.Channels.Add(channel);
            await dbContext.SaveChangesAsync();
            if (ChatHub.UserConnections.TryGetValue(user.Id, out var connections))
            {
                foreach (var connection in connections)
                {
                    await chatHub.Clients.Client(connection).ChannelCreated(channel.ToDto());
                    await chatHub.Groups.AddToGroupAsync(connection, channel.Id.ToString());
                }
            }
            return Results.Ok(channel.ToDto());
        });
        app.UseSerilogRequestLogging();
        app.Run();
    }
}