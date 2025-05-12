package pwr.barwa.chat.ui.layout

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Share
import androidx.compose.material3.BottomAppBar
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ComposableTarget
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import de.charlex.compose.BottomAppBarSpeedDialFloatingActionButton
import de.charlex.compose.FloatingActionButtonItem
import de.charlex.compose.SubSpeedDialFloatingActionButtons
import de.charlex.compose.rememberSpeedDialFloatingActionButtonState
import pwr.barwa.chat.Chats
import pwr.barwa.chat.Debug
import pwr.barwa.chat.Greeting
import pwr.barwa.chat.GreetingRoute
import pwr.barwa.chat.ui.theme.ChatTheme

@OptIn(ExperimentalMaterial3Api::class, ExperimentalAnimationApi::class)
@Composable
fun AuthenticatedLayout(
    navController: NavController,
    content: @Composable () -> Unit
) {
    var fabState = rememberSpeedDialFloatingActionButtonState()
    ChatTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController
                )
            },
            topBar = {
                TopAppBar(
                    title = { Text("Chat App") }
                )
            },
            floatingActionButton = {
                    BottomAppBarSpeedDialFloatingActionButton(
                        state = fabState
                    ) {
                        Icon(Icons.Default.Add, contentDescription = null)
                    }

            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            Surface(
                modifier = Modifier.padding(it).fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {

                Box(
                    modifier = Modifier
                        .padding(start = 16.dp)
                        .fillMaxSize()
                ) {
                    content()
                }
            }
            SubSpeedDialFloatingActionButtons(
                state = fabState,
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                items = listOf(
                    FloatingActionButtonItem(
                        icon = Icons.Default.Person,
                        label = "Person"
                    ) {
                        //TODO onClick
                    },
                    FloatingActionButtonItem(
                        icon = Icons.Default.Home,
                        label = "Home"
                    ) {
                        //TODO onClick
                    }
                )
            )
        }
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

    val items = listOf(
        NavigationItem(
            title = "Chaty",
            icon = Icons.Default.Home,
            route = Chats
        ),
        NavigationItem(
            title = "Home1",
            icon = Icons.Default.AccountBox,
            route = GreetingRoute("Barwa")
        ),
        NavigationItem(
            title = "Debug",
            icon = Icons.Default.Person,
            route = Debug
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
                    navController.navigate(item.route)
                }
            )
        }
    }
}

data class NavigationItem(
    val title: String,
    val icon: ImageVector,
    val route: Any
)