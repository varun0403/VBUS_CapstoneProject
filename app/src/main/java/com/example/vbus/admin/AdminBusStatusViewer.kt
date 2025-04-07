package com.example.vbus.admin

import android.content.Intent
import android.net.Uri
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await
import com.example.vbus.driver.getCurrentDate

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun AdminBusStatusScreen() {
    val db = FirebaseFirestore.getInstance()
    val context = LocalContext.current
    val currentDate = getCurrentDate() // Assume you have this implemented already

    var fullBusList by remember { mutableStateOf<List<BusInfo>>(emptyList()) }
    var errorMessage by remember { mutableStateOf("") }
    var refreshTrigger by remember { mutableStateOf(0) }
    var selectedRoute by remember { mutableStateOf("All") }

    LaunchedEffect(refreshTrigger) {
        try {
            val busesSnapshot = db.collection("buses").get().await()
            val statusSnapshot = db.collection("bus_status").get().await()

            val statusMap = statusSnapshot.documents.associateBy { it.id }

            fullBusList = busesSnapshot.documents.mapNotNull { doc ->
                val busNo = doc.id
                val driverName = doc.getString("driver_name") ?: return@mapNotNull null
                val driverContact = doc.get("driver_contact").toString()
                val routeId = doc.getString("route_id") ?: "Unknown"
                val todayStatus = statusMap[busNo]?.get(currentDate) as? Map<*, *>
                val isOperable = todayStatus?.get("bus_operable") as? Boolean ?: false
                val count = (todayStatus?.get("count") as? Long ?: 0L).toInt()
                val upcoming_stop = todayStatus?.get("upcoming_stop") as? String ?: "loading....."
                BusInfo(busNo, driverName, driverContact, isOperable, count, routeId, upcoming_stop)
            }
        } catch (e: Exception) {
            e.printStackTrace()
            errorMessage = "Failed to load today's bus data."
        }
    }

    val routeOptions = remember(fullBusList) {
        listOf("All") + fullBusList.map { it.routeId }.distinct()
    }

    val filteredBusList = remember(fullBusList, selectedRoute) {
        if (selectedRoute == "All") fullBusList else fullBusList.filter { it.routeId == selectedRoute }
    }

    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("Bus Status", style = MaterialTheme.typography.titleLarge)
            IconButton(onClick = { refreshTrigger++ }) {
                Icon(Icons.Default.Refresh, contentDescription = "Refresh")
            }
        }

        Spacer(Modifier.height(8.dp))

        // Route filter dropdown
        var expanded by remember { mutableStateOf(false) }
        Box {
            Button(onClick = { expanded = true }) {
                Text("Route: $selectedRoute")
            }
            DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
                routeOptions.forEach { route ->
                    DropdownMenuItem(text = { Text(route) }, onClick = {
                        selectedRoute = route
                        expanded = false
                    })
                }
            }
        }

        Spacer(Modifier.height(8.dp))

        if (errorMessage.isNotEmpty()) {
            Text(text = errorMessage, color = MaterialTheme.colorScheme.error)
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(filteredBusList) { bus ->
                BusCard(bus)
            }
        }
    }
}

@Composable
fun BusCard(bus: BusInfo) {
    val context = LocalContext.current
    val bgColor = if (bus.isOperable)
        Color(0xFFD0F0C0) else Color(0xFFFFC2C2)

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(14.dp),
        colors = CardDefaults.cardColors(containerColor = bgColor)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Bus No: ${bus.busNo}", style = MaterialTheme.typography.titleMedium)
            Text("Driver: ${bus.driverName}")

            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Contact: ${bus.driverContact}")
                IconButton(onClick = {
                    val intent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:${bus.driverContact}")
                    }
                    context.startActivity(intent)
                }) {
                    Icon(Icons.Default.Phone, contentDescription = "Call Driver")
                }
            }

            Text("Boarded Count: ${bus.boardedCount}")
            Text("Upcoming stop: ${bus.upcoming_stop}")
            Text("Status: ${if (bus.isOperable) "Operable ✅" else "Inoperable ❌"}")
        }
    }
}

// Add this data class

data class BusInfo(
    val busNo: String,
    val driverName: String,
    val driverContact: String,
    val isOperable: Boolean,
    val boardedCount: Int,
    val routeId: String,
    val upcoming_stop: String
)
