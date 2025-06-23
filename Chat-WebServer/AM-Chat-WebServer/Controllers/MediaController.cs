using System.Threading.Tasks;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.StaticFiles;

namespace AM_Chat_WebServer.Controllers;

[ApiController]
[Route("[controller]")]
public class MediaController(MediaService mediaService) : ControllerBase
{
    [HttpGet("{mediaId}")]
    public async Task<IActionResult> GetMedia(string mediaId)
    {
        var file = mediaService.GetMedia(mediaId);
        if (file != null)
        {
            var provider = new FileExtensionContentTypeProvider();
            string contentType;
            if (!provider.TryGetContentType(file.Name, out contentType))
            {
                contentType = "application/octet-stream"; // domyślny typ, jeśli nie znaleziono
            }
            
            return File(file, contentType);
        }

        return NotFound();
    }
    
    [Authorize]
    [HttpPost]
    public async Task<IActionResult> ChangeAvatar([FromForm] IFormFile file)
    {
        if (file == null || file.Length == 0)
        {
            return BadRequest("No file uploaded.");
        }

        var userId = User.FindFirst("id")?.Value;
        if (userId == null)
        {
            return Unauthorized();
        }

        var user = await mediaService.ChangeAvatar(file, long.Parse(userId));
        if (user == null)
        {
            return NotFound();
        }

        return Ok(user.ToDto());
    }
}