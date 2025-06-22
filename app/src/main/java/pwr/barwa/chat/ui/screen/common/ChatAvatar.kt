package pwr.barwa.chat.ui.screen.common

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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.AsyncImage
import pwr.barwa.chat.data.dto.ChannelDto

@Composable
fun ChatAvatar(chat: ChannelDto, modifier: Modifier = Modifier) {
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
                contentDescription = "Awatar czatu",
                modifier = Modifier.fillMaxSize(),
                contentScale = ContentScale.Crop
            )
        } else {
            val icon = Icons.Default.Person
            Icon(
                imageVector = icon,
                contentDescription = "Awatar czatu",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
