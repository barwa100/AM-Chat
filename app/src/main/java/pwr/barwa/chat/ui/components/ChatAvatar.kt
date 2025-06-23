package pwr.barwa.chat.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pwr.barwa.chat.data.dto.ChannelDto

@Composable
fun ChatAvatar(chat: ChannelDto, modifier: Modifier = Modifier) {
    val avatarColor = remember(chat.id) {
        val random = java.util.Random(chat.id)

        val red = 0.3f + random.nextFloat() * 0.6f
        val green = 0.3f + random.nextFloat() * 0.6f
        val blue = 0.3f + random.nextFloat() * 0.6f

        Color(red, green, blue)
    }

    Box(
        modifier = modifier
            .size(48.dp)
            .clip(CircleShape)
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        if (!chat.image.isNullOrEmpty()) {
            AsyncImage(
                model = chat.image,
                contentDescription = "Chat avatar",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            // Fallback icon when no image is available
            val icon = if (chat.members.size > 2) Icons.Default.Person else Icons.Default.Person
            Icon(
                imageVector = icon,
                contentDescription = "Chat avatar",
                tint = avatarColor,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
