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
}