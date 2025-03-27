package com.example.vbus.student

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.maps.android.compose.Circle
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.rememberCameraPositionState

//class MapsTest : ComponentActivity() {
//    override fun onCreate(savedInstanceState: Bundle?) {
//        super.onCreate(savedInstanceState)
//        setContent {
//            MapTestScreen()
//        }
//    }
//}

@Composable
fun MapTestScreen(bus_no: String) {
    val db = Firebase.firestore

    // MutableState to hold geofence centers
    val geofenceCenters = remember { mutableStateListOf<LatLng>() }
    val geofenceRadius = 130.0
    Log.d("Bus no",bus_no)
    // Fetch data from Firestore
    LaunchedEffect(Unit) {
        val documentRef = db.collection("buses").document(bus_no)
        documentRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    Log.d("FirestoreData", "Document fetched: $document")
                    val stops = document.get("stops") as? Map<*, *>
                    if (stops != null) {
                        Log.d("FirestoreData", "Stops found: $stops")
                        stops.forEach { (key, value) ->
                            val geoPoint = value as? com.google.firebase.firestore.GeoPoint
                            if (geoPoint != null) {
                                geofenceCenters.add(LatLng(geoPoint.latitude, geoPoint.longitude))
                                Log.d(
                                    "FirestoreData",
                                    "Added GeoPoint for key=$key: ${geoPoint.latitude}, ${geoPoint.longitude}"
                                )
                            } else {
                                Log.d("FirestoreData", "Invalid GeoPoint for key=$key: $value")
                            }
                        }
                        Log.d("FirestoreData", "Final geofence centers: $geofenceCenters")
                    } else {
                        Log.d("FirestoreData", "No 'stops' field found in document.")
                    }
                } else {
                    Log.d("FirestoreData", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error fetching document", exception)
            }
    }

    // Default camera position
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(
            LatLng(13.088046, 80.179506), // Use the first geofence or fallback
            15f
        )
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true)
    ) {
        // Add circles dynamically as geofenceCenters is updated
        geofenceCenters.forEach { center ->
            Log.d(center.latitude.toString(),center.longitude.toString())
            Circle(
                center = center,
                radius = geofenceRadius, // Radius in meters
                strokeColor = Color.Gray,
                fillColor = Color.Blue.copy(alpha = 0.3f),
                strokeWidth = 2f
            )
        }
    }
}
