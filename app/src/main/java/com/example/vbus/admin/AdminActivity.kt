package com.example.vbus.admin

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vbus.notifications.MyFirebaseMessagingService
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import com.example.vbus.student.MenuItem
import com.example.vbus.student.CustomCard

data class MenuItem(val text: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun AdminHomeScreen(navController: NavController, name: String) {
    val context = LocalContext.current
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    var backPressedOnce by remember { mutableStateOf(false) }

    BackHandler {
        if (backPressedOnce) {
            (context as Activity).finish()
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()

            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }

    val menuItems = listOf(
        MenuItem("Logout", Icons.Default.AccountBox) {
            auth.signOut()
            navController.navigate("auth") { popUpTo(0) }
        },
        MenuItem("Test Notification", Icons.Default.AccountBox) {
            MyFirebaseMessagingService.showNotification(
                context,
                "Test Notification",
                "This is a test message."
            )
        },
        MenuItem("Complaints", Icons.Default.AccountBox) { navController.navigate("admin_complaint_home") },
        MenuItem("Announcements", Icons.Default.AccountBox) { navController.navigate("admin_announcement") },
        MenuItem("Student Details", Icons.Default.AccountBox) { navController.navigate("fetch_student_data") },
        MenuItem("Camera Test", Icons.Default.AccountBox) { navController.navigate("camera_screen") },
        MenuItem("Track Buses", Icons.Default.AccountBox) { navController.navigate("admin_tracking") }
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Admin Home Screen")
        Text(text = "Welcome $name")

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
            items(menuItems){ item->
                CustomCard(item.text, item.icon, item.onClick)
            }
        }
    }
}
