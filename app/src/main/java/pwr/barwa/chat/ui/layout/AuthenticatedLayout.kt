package pwr.barwa.chat.ui.layout

import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.navigation.Navigation.findNavController
import com.leinardi.android.speeddial.compose.FabWithLabel
import com.leinardi.android.speeddial.compose.SpeedDial
import com.leinardi.android.speeddial.compose.SpeedDialOverlay
import com.leinardi.android.speeddial.compose.SpeedDialState
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
    var speedDialState = rememberSaveable { mutableStateOf(SpeedDialState.Collapsed) }
    var overlayVisible = rememberSaveable { mutableStateOf(speedDialState.value.isExpanded()) }
    ChatTheme {
        Scaffold(
            bottomBar = {
                BottomNavigationBar(
                    navController = navController
                )
//                BottomAppBar(
//                    actions = {
//                        //Add 3 buttons, "Home", "Person", "Add"
//                        Icon(
//                            imageVector = Icons.Default.Home,
//                            contentDescription = "Home",
//                            modifier = Modifier.padding(16.dp)
//                        )
//                        Icon(
//                            imageVector = Icons.Default.Person,
//                            contentDescription = "Person",
//                            modifier = Modifier.padding(16.dp)
//                        )
//                        Icon(
//                            imageVector = Icons.Default.Add,
//                            contentDescription = "Add",
//                            modifier = Modifier.padding(16.dp)
//                        )
//                    },
//                    floatingActionButton = {
//                        BottomAppBarSpeedDialFloatingActionButton(
//                            state = fabState
//                        ) {
//                            Icon(Icons.Default.Add, contentDescription = null)
//                        }
//                    }
//                )
            },
            topBar = {
                TopAppBar(
                    title = { Text("Chat App") }
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = { /* Akcja FAB */ },
                    containerColor = MaterialTheme.colorScheme.primary
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Dodaj")
                }
//                    SubSpeedDialFloatingActionButtons(
//                        state = fabState,
//                        items = listOf(
//                            FloatingActionButtonItem(
//                                icon = Icons.Default.Person,
//                                label = "Person"
//                            ) {
//                                //TODO onClick
//                            },
//                            FloatingActionButtonItem(
//                                icon = Icons.Default.Home,
//                                label = "Home"
//                            ) {
//                                //TODO onClick
//                            }
//                        )
//                    )
//                    Spacer(modifier = Modifier.height(16.dp))
//                    BottomAppBarSpeedDialFloatingActionButton(
//                        state = fabState
//                    ) {
//                        Icon(Icons.Default.Add, contentDescription = null)
//                    }

            },
            containerColor = MaterialTheme.colorScheme.background
        ) {
            SpeedDialOverlay(
                visible = overlayVisible.value,
                onClick = {
                    overlayVisible.value = false
                    speedDialState.value = speedDialState.value.toggle()
                },
            )
            Surface(
                modifier = Modifier.padding(it).fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(PaddingValues(0.dp))
                ) {
                    content()
                }
            }
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
            title = "Home1",
            icon = Icons.Default.Home,
            route = GreetingRoute("Test")
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