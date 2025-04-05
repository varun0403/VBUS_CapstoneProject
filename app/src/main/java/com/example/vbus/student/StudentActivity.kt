package com.example.vbus.student

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import com.google.firebase.firestore.firestore
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.foundation.Image
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.ui.platform.LocalContext
import com.example.vbus.R
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun StudentHomeScreen(navController: NavController, email: String) {
    val auth: FirebaseAuth = Firebase.auth
    val db = Firebase.firestore
    val docRef = db.collection("students").document(email)
    var studentName by remember { mutableStateOf("Fetching name...") }
    var busNo by remember { mutableStateOf("Loading...") }
    var regNo by remember { mutableStateOf("Fetching reg no...") }
    var backPressedOnce by remember { mutableStateOf(false) }
    val context = LocalContext.current


    BackHandler {
        if (backPressedOnce) {
            (context as Activity).finish() // Exit app
        } else {
            backPressedOnce = true
            Toast.makeText(context, "Press again to exit", Toast.LENGTH_SHORT).show()

            // Reset flag after 2 seconds
            CoroutineScope(Dispatchers.Main).launch {
                delay(2000)
                backPressedOnce = false
            }
        }
    }

    LaunchedEffect(email) {
        docRef.get().addOnSuccessListener { document ->
            studentName = document.getString("student_name").orEmpty()
            busNo = document.getString("bus_no").orEmpty()
            regNo = document.getString("reg_no").orEmpty()
        }
    }

    LaunchedEffect(Unit) {
        FirebaseMessaging.getInstance().subscribeToTopic(busNo)
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Top,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Welcome, $studentName", fontSize = 24.sp, fontWeight = FontWeight.Bold)

        Spacer(modifier = Modifier.height(8.dp))
        Text(text = "Bus No: $busNo", fontSize = 16.sp)
        Text(text = "Reg No: $regNo", fontSize = 16.sp)

        Spacer(modifier = Modifier.height(16.dp))

        // Grid Layout for Navigation Cards
        val menuItems = listOf(
            MenuItem("Announcements", Icons.Default.AccountBox) { navController.navigate("student_announcement") },
            MenuItem("Complaints", Icons.Default.AccountBox) { navController.navigate("complaint_screen/$email") },
            MenuItem("Download Letter", Icons.Default.AccountBox) { navController.navigate("letter_generation/$email") },
            MenuItem("Track My Bus", Icons.Default.AccountBox) { navController.navigate("track_bus/$busNo/$email") },
            MenuItem("Where is My Bus?", Icons.Default.AccountBox) { navController.navigate("map_test_screen/$busNo") },
            MenuItem("Logout", Icons.Default.AccountBox) {
                auth.signOut()
                navController.navigate("auth") { popUpTo(0) }
            }
        )

        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
            items(menuItems) { item ->
                CustomCard(item.text, item.icon, item.onClick)
            }
        }
    }
}

// Data class for menu items
data class MenuItem(val text: String, val icon: ImageVector, val onClick: () -> Unit)

@Composable
fun CustomCard(text: String, icon: ImageVector, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primary), // Custom color
        modifier = Modifier
            .padding(8.dp)
            .fillMaxWidth(0.9f)
            .height(120.dp),
        elevation = CardDefaults.cardElevation(8.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Icon(imageVector = icon, contentDescription = text, modifier = Modifier.size(40.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = text, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

