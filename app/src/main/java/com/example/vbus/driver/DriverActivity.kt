package com.example.vbus.driver

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.NavHostController
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

    // UI
    Column {
        Text(text = "Welcome driver ${driverName.value}")
        Text(text = "Bus no: ${bus_no.value}")
        Button(
            onClick = { navController.navigate("driver_loc/${bus_no.value}") }
        ) {
            Text(text = "Start Trip")
        }
        Button(
            onClick = { navController.navigate("breakdown_resolution") }
        ) {
            Text(text = "Bus Breakdown")
        }
    }
}
