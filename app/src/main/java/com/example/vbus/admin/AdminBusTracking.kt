package com.example.vbus.admin

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.database.*
import com.google.maps.android.compose.*

@Composable
fun AdminMapScreen() {
    // Firebase Database reference for all bus locations
    val databaseReference = remember {
        FirebaseDatabase.getInstance("https://vbus-160e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("driverLocation")
    }

    // State to store multiple bus locations
    var busLocations by remember { mutableStateOf<Map<String, LatLng>>(emptyMap()) }

    // Fetch all bus locations
    LaunchedEffect(Unit) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val locations = mutableMapOf<String, LatLng>()
                snapshot.children.forEach { busSnapshot ->
                    val busNo = busSnapshot.key ?: "Unknown"
                    val latitude = busSnapshot.child("latitude").getValue(Double::class.java)
                    val longitude = busSnapshot.child("longitude").getValue(Double::class.java)

                    if (latitude != null && longitude != null) {
                        locations[busNo] = LatLng(latitude, longitude)
                    }
                }
                busLocations = locations
            }

            override fun onCancelled(error: DatabaseError) {
                // Handle database read error (optional)
            }
        })
    }

    // Display map with all bus markers
    GoogleMapWithMultipleMarkers(busLocations)
}

@Composable
fun GoogleMapWithMultipleMarkers(busLocations: Map<String, LatLng>) {
    val defaultLocation = LatLng(12.8406, 80.1536) // Example: VIT Chennai coordinates

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLocation, 15f) // Adjust zoom as needed
    }

    LaunchedEffect(busLocations) {
        if (busLocations.isNotEmpty()) {
            // Move camera to the first bus's location
            val firstBusLocation = busLocations.values.first()
            cameraPositionState.animate(CameraUpdateFactory.newLatLngZoom(firstBusLocation, 15f))
        }
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true)
    ) {
        busLocations.forEach { (busNo, location) ->
            Marker(
                state = rememberMarkerState(position = location),
                title = "Bus $busNo",
                snippet = "Location: ${location.latitude}, ${location.longitude}"
            )

            Circle(
                center = location,
                radius = 150.0,
                strokeColor = Color.Blue,
                fillColor = Color.Blue.copy(alpha = 0.2f),
                strokeWidth = 2f
            )
        }
    }
}

