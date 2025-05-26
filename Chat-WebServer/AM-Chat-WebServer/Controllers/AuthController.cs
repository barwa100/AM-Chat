using System.Security.Claims;
using System.Text;
using AM_Chat_WebServer.Data;
using Microsoft.AspNetCore.Authentication;
using Microsoft.AspNetCore.Authentication.BearerToken;
using Microsoft.AspNetCore.Http.HttpResults;
using Microsoft.AspNetCore.Mvc;

namespace AM_Chat_WebServer.Controllers;

[ApiController]
[Route("[controller]")]
public class AuthController(ChatDbContext dbContext) : ControllerBase
{
    [HttpPost("login")]
    public async Task<Results<Ok<AccessTokenResponse>, EmptyHttpResult, ProblemHttpResult>> Login([FromBody] AccountRequest request)
    {
        var user = dbContext.Users.FirstOrDefault(u => u.UserName == request.Username && u.Password == request.Password);
        if (user == null)
        {
            return TypedResults.Problem("Invalid email or password.", statusCode: StatusCodes.Status401Unauthorized);
        }
        await HttpContext.SignInAsync(
            BearerTokenDefaults.AuthenticationScheme,
            new ClaimsPrincipal(new ClaimsIdentity(new[]
            {
                new Claim(ClaimTypes.NameIdentifier, user.Id.ToString()),
                new Claim(ClaimTypes.Name, user.UserName)
            }, BearerTokenDefaults.AuthenticationScheme)),
            new AuthenticationProperties
            {
                IsPersistent = true,
                ExpiresUtc = DateTimeOffset.UtcNow.AddHours(12)
            });
        return TypedResults.Empty;
    }
    
    [HttpPost("logout")]
    public async Task<Results<Ok, EmptyHttpResult, ProblemHttpResult>> Logout()
    {
        await HttpContext.SignOutAsync(BearerTokenDefaults.AuthenticationScheme);
        return TypedResults.Ok();
    }
    
    [HttpPost("register")]
    public async Task<Results<Ok, EmptyHttpResult, ProblemHttpResult>> Register([FromBody]AccountRequest request)
    {
        if (dbContext.Users.Any(u => u.UserName == request.Username))
        {
            return TypedResults.Problem("Istnieje użytkownik o takiej nazwie użytkownika.", statusCode: StatusCodes.Status400BadRequest);
        }
        var user = new Data.Models.User
        {
            UserName = request.Username,
            Password = request.Password
        };
        dbContext.Users.Add(user);
        await dbContext.SaveChangesAsync();
        return TypedResults.Ok();
    }
    
}

public record AccountRequest(string Username, string Password);