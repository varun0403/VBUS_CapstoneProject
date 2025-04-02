package com.example.vbus.student

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.vbus.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.firebase.Firebase
import com.google.firebase.database.*
import com.google.firebase.firestore.firestore
import com.google.maps.android.compose.*
import com.example.vbus.driver.getCurrentDate
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptorFactory

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun MyMapScreen(bus_no: String, email: String) {
    val databaseReference = remember {
        FirebaseDatabase.getInstance("https://vbus-160e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
            .getReference("driverLocation").child(bus_no)
    }
    val db = Firebase.firestore
    val docRef = db.collection("bus_status").document(bus_no)

    var busOperable by remember { mutableStateOf(true) }
    var rescueReq by remember { mutableStateOf(false) }
    var driverLocation by remember { mutableStateOf<LatLng?>(null) }
    var busStatus by remember { mutableStateOf("Fetching status...") }
    var hasBoarded by remember { mutableStateOf(false) }

    // Fetch driver location from Realtime Database
    LaunchedEffect(Unit) {
        databaseReference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val latitude = snapshot.child("latitude").getValue(Double::class.java)
                val longitude = snapshot.child("longitude").getValue(Double::class.java)
                if (latitude != null && longitude != null) {
                    driverLocation = LatLng(latitude, longitude)
                }
                busStatus = snapshot.child("status").getValue(String::class.java) ?: "Unknown"
            }
            override fun onCancelled(error: DatabaseError) {}
        })
    }

    // Fetch Firestore bus details and boarding status
    LaunchedEffect(Unit) {
        docRef.get()
            .addOnSuccessListener { document ->
                val date_ = getCurrentDate()
                val busDetails = document.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
                busOperable = busDetails["bus_operable"] as? Boolean ?: false
                rescueReq = busDetails["rescue_request"] as? Boolean ?: false
            }

        HasBoarded(bus_no, email) { isBoarded ->
            hasBoarded = isBoarded
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Map occupies 80%
        Box(modifier = Modifier.weight(0.8f)) {
            driverLocation?.let { location ->
                GoogleMapWithMarker(location)
            } ?: LoadingScreen()
        }

        // Bus status occupies 20%
        Box(
            modifier = Modifier
                .weight(0.2f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(text = "Bus Operable: $busOperable")
                Text(text = "Rescue Request: $rescueReq")
                Text(text = if (hasBoarded) "You have boarded the bus" else "You are waiting for the bus")
            }
        }
    }
}

// Now correctly used inside `MyMapScreen`
@RequiresApi(Build.VERSION_CODES.O)
fun HasBoarded(bus_no: String, email: String, callback: (Boolean) -> Unit) {
    val db = Firebase.firestore
    val docRef = db.collection("bus_status").document(bus_no)

    docRef.get()
        .addOnSuccessListener { document ->
            val date_ = getCurrentDate()
            val boardedStudents = document.get("$date_.students_boarded") as? List<String> ?: emptyList()
            callback(email in boardedStudents) // Return true if found, else false
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching data", e)
            callback(false) // Default to false if there's an error
        }
}




@Composable
fun GoogleMapWithMarker(driverLocation: LatLng) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(driverLocation, 15f)
    }
    val markerState = rememberMarkerState(position = driverLocation)

    LaunchedEffect(driverLocation) {
        markerState.position = driverLocation
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(driverLocation))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true)
    ) {
        Marker(
            state = markerState,
            title = "Driver Location",
            snippet = "Current location of the driver",
        )

        Circle(
            center = driverLocation,
            radius = 500.0,
            strokeColor = Color.Red,
            fillColor = Color.Red.copy(alpha = 0.2f),
            strokeWidth = 2f
        )
    }
}

@Composable
fun LoadingScreen() {
    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        BasicText(text = "Fetching driver's location...")
    }
}
