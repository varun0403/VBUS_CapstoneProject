package com.example.vbus.driver

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Place
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
import com.example.vbus.notifications.MyFirebaseMessagingService
import com.example.vbus.student.CustomCard
import com.example.vbus.student.MenuItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun DriverHomeScreen(navController: NavHostController, driver_email: String) {
    val db = Firebase.firestore
    val driver_doc = db.collection("drivers").document(driver_email)
    val auth: FirebaseAuth = FirebaseAuth.getInstance()
    // State to hold the driver's name
    val driverName = remember { mutableStateOf("Fetching driver name...") }
    val bus_no = remember { mutableStateOf("Fetching bus number...") }

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

    LaunchedEffect(driver_email) {
        driver_doc.get()
            .addOnSuccessListener { document ->
                driverName.value = document.getString("driver_name").orEmpty()
                bus_no.value = document.getString("bus_no").orEmpty()
            }
            .addOnFailureListener {
                driverName.value = "Error fetching driver name"
            }
    }
    val menuItems = listOf(
        MenuItem("Logout", Icons.Default.Home) {
            auth.signOut()
            navController.navigate("auth") { popUpTo(0) }
        },
        MenuItem("Start Trip", Icons.Default.Place) {
            navController.navigate("driver_loc/${bus_no.value}")
        },
        MenuItem("Contact Admin", Icons.Default.Call) {
            val intent = Intent(Intent.ACTION_DIAL).apply {
                data = Uri.parse("tel:9843261840")
            }
            context.startActivity(intent)
        })
    Column {
        Text(text = "Welcome driver ${driverName.value}")
        Text(text = "Bus no: ${bus_no.value}")
        LazyVerticalGrid(columns = GridCells.Fixed(2), modifier = Modifier.fillMaxSize()) {
            items(menuItems){ item->
                CustomCard(item.text, item.icon, item.onClick)
            }
        }
    }
}
