package com.example.vbus.student

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
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
    var rescue_bus by remember { mutableStateOf("") }
    var upcoming_stop by remember { mutableStateOf("loading") }

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
                upcoming_stop = busDetails["upcoming_stop"] as? String?: "NA"
            }

        HasBoarded(bus_no, email) { isBoarded ->
            hasBoarded = isBoarded
        }

        rescue_bus_no(bus_no, email) { allottedBus ->
            rescue_bus = allottedBus // this will be "0" or a bus number like "BUS_102"
        }

    }

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Map occupies 80%
        Box(modifier = Modifier.weight(0.7f)) {
            driverLocation?.let { location ->
                GoogleMapWithMarker(location)
            } ?: LoadingScreen()
        }

        // Bus status occupies 20%
        Box(
            modifier = Modifier
                .weight(0.3f)
                .fillMaxWidth()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {

                // Bus Status Card (Running / Breakdown)
                Card(
                    colors = CardDefaults.cardColors(
                        containerColor = if (busOperable) Color(0xFFDFFFD6) else Color(0xFFFFD6D6)
                    ),
                    modifier = Modifier
                        .padding(8.dp)
                        .fillMaxWidth(0.9f)
                ) {
                    Text(
                        text = if (busOperable) "Running" else "Breakdown",
                        color = if (busOperable) Color(0xFF2E7D32) else Color(0xFFC62828),
                        modifier = Modifier.padding(12.dp),
                        fontWeight = FontWeight.Bold
                    )
                }

                // Two in a row: Rescue Request and Rescue Bus No
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Rescue Request Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFFFF59D) // yellow-ish
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Rescue Req: $rescueReq",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Rescue Bus Number Card
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (rescue_bus == "0") Color.LightGray else Color(0xFFBBDEFB) // Grey or Blue
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Rescue bus no: $rescue_bus",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                // Two in a row: Upcoming Stop and Boarding Status
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly
                ) {
                    // Upcoming Stop
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = Color(0xFFE3F2FD) // Light blue-ish
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = "Upcoming stop: $upcoming_stop",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    // Boarding status
                    Card(
                        colors = CardDefaults.cardColors(
                            containerColor = if (hasBoarded) Color(0xFFC8E6C9) else Color(0xFFFFF59D) // Green or Yellow
                        ),
                        modifier = Modifier
                            .padding(8.dp)
                            .weight(1f)
                    ) {
                        Text(
                            text = if (hasBoarded) "Boarded" else "Not boarded",
                            modifier = Modifier.padding(12.dp),
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
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
            val boardedStudents = document.get("$date_.allocated_buses") as? Map<*, *> ?: emptyMap<Any, Any>()
            callback(email in boardedStudents.keys.toList()) // Return true if found, else false
        }
        .addOnFailureListener { e ->
            Log.e("Firestore", "Error fetching data", e)
            callback(false) // Default to false if there's an error
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun rescue_bus_no(bus_no: String, email: String, callback: (String) -> Unit) {
    val db = Firebase.firestore
    val docRef = db.collection("bus_status").document(bus_no)
    val date_ = getCurrentDate()

    docRef.get()
        .addOnSuccessListener { document ->
            // Check if bus is operable
            val busOperable = document.getBoolean("$date_.bus_operable") ?: true
            if (busOperable) {
                // Bus is fine, show 0
                callback("0")
                return@addOnSuccessListener
            }

            // Now check allocated buses
            val allocatedBuses = document.get("$date_.allocated_buses") as? Map<*, *> ?: emptyMap<Any, Any>()
            val allottedBus = allocatedBuses[email] as? String ?: "0"
            callback(allottedBus)
        }
        .addOnFailureListener {
            // Firestore fetch failed, just return 0
            callback("0")
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
