package pwr.barwa.chat.ui.screen

import androidx.compose.runtime.Composable

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pwr.barwa.chat.data.dto.UserDto
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.MyProfileViewModel


@Composable
fun MyProfileScreen(
    viewModel: MyProfileViewModel = viewModel(factory = AppViewModelProvider.Factory),
    onChangePasswordClick: () -> Unit,
    onChangeAvatarClick: () -> Unit
) {
    val user by viewModel.user.collectAsState()
    var profileDescription by remember { mutableStateOf("") }

    user?.let { nonNullUser ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Avatar
            Box(
                modifier = Modifier
                    .size(128.dp)
                    .clip(CircleShape)
                    .clickable { onChangeAvatarClick() },
                contentAlignment = Alignment.Center
            ) {
                if (!nonNullUser.avatarUrl.isNullOrBlank()) {
                    AsyncImage(
                        model = ImageRequest.Builder(LocalContext.current)
                            .data(nonNullUser.avatarUrl)
                            .crossfade(true)
                            .build(),
                        contentDescription = "User Avatar",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(Color.Gray)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Username
            Text(
                text = nonNullUser.userName,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Profile description
            OutlinedTextField(
                value = profileDescription,
                onValueChange = { profileDescription = it },
                label = { Text("Profile Description") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Change password
            Button(onClick = onChangePasswordClick) {
                Text("Change Password")
            }
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
