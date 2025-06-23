package pwr.barwa.chat.ui.screen

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable

import androidx.compose.foundation.background
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import pwr.barwa.chat.ui.AppViewModelProvider
import pwr.barwa.chat.ui.MyProfileViewModel
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import pwr.barwa.chat.services.AuthService

@Composable
fun MyProfileScreen(
    viewModel: MyProfileViewModel = viewModel(factory = AppViewModelProvider.Factory)
) {
    val user by viewModel.user.collectAsState()
    val isAvatarUpdating by viewModel.isAvatarUpdating.collectAsState()
    val errorMessage by viewModel.errorMessage.collectAsState()
    var profileDescription by remember { mutableStateOf("") }
    val context = LocalContext.current

    // Launcher do wyboru obrazu z galerii
    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            viewModel.onChangeAvatar(it)
        }
    }

    user?.let { nonNullUser ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Wyświetlanie awatara z przyciskiem edycji
            Box(
                modifier = Modifier.size(140.dp),
                contentAlignment = Alignment.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(128.dp)
                        .clip(CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (!nonNullUser.avatarUrl.isNullOrBlank()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(if (nonNullUser.avatarUrl.startsWith("http"))
                                        nonNullUser.avatarUrl
                                      else
                                        "${AuthService.URL_BASE}${nonNullUser.avatarUrl}")
                                .crossfade(true)
                                .build(),
                            contentDescription = "Awatar użytkownika",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )
                    }
                }

                // Przycisk edycji awatara
                FloatingActionButton(
                    onClick = { imagePickerLauncher.launch("image/*") },
                    modifier = Modifier
                        .size(40.dp)
                        .align(Alignment.BottomEnd),
                    containerColor = MaterialTheme.colorScheme.primary,
                    contentColor = MaterialTheme.colorScheme.onPrimary
                ) {
                    Icon(
                        imageVector = Icons.Default.Edit,
                        contentDescription = "Edytuj awatar",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Wyświetlanie informacji o aktualizacji awatara
            if (isAvatarUpdating) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Aktualizowanie awatara...",
                    style = MaterialTheme.typography.bodySmall
                )
            }

            // Wyświetlanie błędów
            errorMessage?.let { error ->
                Text(
                    text = error,
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 4.dp)
                )
            }

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
                label = { Text("Opis profilu") },
                modifier = Modifier.fillMaxWidth()
            )
        }
    } ?: Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator()
    }
}
