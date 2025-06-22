package pwr.barwa.chat.ui.layout

import android.media.MediaPlayer
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Divider
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

import pwr.barwa.chat.Chats
import pwr.barwa.chat.Contacts
import pwr.barwa.chat.MyProfile
import pwr.barwa.chat.R
import pwr.barwa.chat.ui.theme.ChatTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthenticatedLayout(
    navController: NavController,
    onLogoutClick: () -> Unit,
    content: @Composable (PaddingValues) -> Unit,
    onBackgroundColorChange: (Color) -> Unit,
    backgroundColor: Color
) {

    var showMenu by remember { mutableStateOf(false) }
    val colorOptions = listOf(
        Color(0xFFffddff), // Light Purple
        Color(0xFFcaddff), // Light Blue
        Color(0xFFFFFFFF), // White
        Color(0xFFced0d2)  // Light Grey
    )

    ChatTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(navController = navController)
            },
            topBar = {
                TopAppBar(
                    title = { Text("Twitter") },
                    actions = {
                        Box {
                            IconButton(onClick = { showMenu = true }) {
                                Icon(
                                    imageVector = Icons.Default.MoreVert,
                                    contentDescription = "Options"
                                )
                            }

                            DropdownMenu(
                                expanded = showMenu,
                                onDismissRequest = { showMenu = false }
                            ) {
                                // Background color options
                                colorOptions.forEach { color ->
                                    DropdownMenuItem(
                                        text = {
                                            Box(
                                                modifier = Modifier
                                                    .fillMaxWidth()
                                                    .height(24.dp)
                                                    .background(color)
                                            )
                                        },
                                        onClick = {
                                            onBackgroundColorChange(color)
                                            showMenu = false
                                        }
                                    )
                                }
                                Divider()
                                // Logout option
                                DropdownMenuItem(
                                    text = { Text("Logout") },
                                    onClick = {
                                        onLogoutClick()
                                        showMenu = false
                                    }
                                )
                            }
                        }
                    }
                )
            },
            content = { scaffoldPadding ->
                Box(
                    modifier = Modifier
                        .padding(scaffoldPadding)
                        .background(backgroundColor) // <-- tutaj ustawiamy kolor tła
                        .fillMaxSize()
                ) {
                    content(scaffoldPadding)
                }
            }
        )
    }
}



@Composable
fun BottomNavigationBar(
    navController: NavController
)
{
    val selectedNavigationIndex = rememberSaveable {
        mutableIntStateOf(0)
    }
    val context = LocalContext.current
    // Utwórz MediaPlayer
    val mediaPlayer = remember { MediaPlayer.create(context, R.raw.click) }

    val items = listOf(
        NavigationItem(
            title = "Chaty",
            icon = Icons.Default.Home,
            route = Chats
        ),
        NavigationItem(
            title = "Mój Profil",
            icon = Icons.Default.AccountBox,
            route = MyProfile
        ),
        NavigationItem(
            title = "Kontakty",
            icon = Icons.Default.AccountBox,
            route = Contacts
        )
    )

    NavigationBar {
        items.forEachIndexed { index, item ->
            NavigationBarItem(
                icon = {
                    Icon(
                        imageVector = item.icon,
                        contentDescription = null
                    )
                },
                label = {
                    Text(
                        item.title,
                        color = if (index == selectedNavigationIndex.intValue)
                            Color.Black
                        else Color.Gray
                    )
                },
                selected = selectedNavigationIndex.intValue == index,
                onClick = {
                    selectedNavigationIndex.intValue = index
                    // Odtwórz dźwięk przed nawigacją
                    mediaPlayer.seekTo(0) // Przewiń do początku jeśli dźwięk był już odtwarzany
                    mediaPlayer.start()

                    navController.navigate(item.route)
                }
            )
        }
    }
    // Sprzątanie przy zniszczeniu komponentu
    DisposableEffect(Unit) {
        onDispose {
            mediaPlayer.release()
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
)