package com.example.vbus.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Looper
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.navigation.NavHostController
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.SetOptions
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlin.math.*
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import androidx.compose.ui.platform.LocalContext //import for notification
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.google.firebase.firestore.FieldValue
import com.example.vbus.routes
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverMapScreen(navController: NavHostController, bus_no: String) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val db = Firebase.firestore
    val geofenceCenters = remember { mutableStateListOf<LatLng>() }
    val checkPoints = remember { mutableStateListOf<LatLng>() }
    val geofenceRadius = 130.0
    val route_id = remember { mutableStateOf("loading.......") }

    var userLocation by remember { mutableStateOf<LatLng?>(null) }
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            startLocationUpdates(bus_no, fusedLocationClient, context, { location ->
                userLocation = location
            }, geofenceCenters, checkPoints, geofenceRadius)
        }
    }

    LaunchedEffect(Unit) {
        db.collection("buses").document(bus_no).get()
            .addOnSuccessListener {
                route_id.value = it.getString("route_id") ?: "NA"
            }
            .addOnFailureListener {
                route_id.value = "NA"
            }
    }

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startLocationUpdates(bus_no, fusedLocationClient, context, { location ->
                userLocation = location
            }, geofenceCenters, checkPoints, geofenceRadius)
        } else {
            locationPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    Column(modifier = Modifier.fillMaxSize()) {
        //  MAP TAKES 82% or 90% OF THE SCREEN
        Box(modifier = Modifier.weight(0.82f).fillMaxWidth()) {
            userLocation?.let { location ->
                GoogleMapWithMarker(location, geofenceCenters, checkPoints, geofenceRadius)
            } ?: LoadingScreen()
        }

        // 🟢 BUTTONS & TEXT TAKES 18% OF THE SCREEN
        Box(
            modifier = Modifier
                .weight(0.18f) // Change to 0.10f if you want 10% space
                .fillMaxWidth()
                .background(Color.White) // Background for clarity
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Bus No: $bus_no",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Route Id: ${route_id.value}",
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.Bold
                )
                Button(
                    onClick = { alertNearbyBuses(bus_no) }
                ) { Text("Report Breakdown") }

                var isRescueRequested by remember { mutableStateOf(false) }
                LaunchedEffect(bus_no) {
                    val docRef = db.collection("bus_status").document(bus_no)
                    docRef.get().addOnSuccessListener { document ->
                        isRescueRequested = document.getBoolean("rescue_request") ?: false
                    }
                }
                Button(
                    onClick = {},
                    enabled = isRescueRequested
                ) { Text("Navigate to broken bus") }
            }
        }
    }
}


@Composable
fun LoadingScreen() {

}

@Composable
fun GoogleMapWithMarker(
    userLocation: LatLng,
    geofenceCenters: List<LatLng>,
    checkPoints: List<LatLng>,
    geofenceRadius: Double
) {
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(userLocation, 15f)
    }

    val markerState = rememberMarkerState(position = userLocation)

    LaunchedEffect(userLocation) {
        markerState.position = userLocation
        cameraPositionState.animate(CameraUpdateFactory.newLatLng(userLocation))
    }

    GoogleMap(
        modifier = Modifier.fillMaxSize(),
        cameraPositionState = cameraPositionState,
        uiSettings = MapUiSettings(compassEnabled = true, zoomControlsEnabled = true)
    ) {
        Marker(
            state = markerState,
            title = "Your Location",
            snippet = "You are here"
        )

        // Draw circles for all geofence centers
        geofenceCenters.forEach { geofenceCenter ->
            Circle(
                center = geofenceCenter,
                radius = geofenceRadius,
                strokeColor = Color.Blue,
                fillColor = Color.Blue.copy(alpha = 0.2f),
                strokeWidth = 2f
            )
        }

        // Draw check points on map
        checkPoints.forEach { geofenceCenter ->
            Circle(
                center = geofenceCenter,
                radius = geofenceRadius,
                strokeColor = Color.Blue,
                fillColor = Color.Blue.copy(alpha = 0.2f),
                strokeWidth = 2f
            )
        }
    }
}


// Start location updates
@RequiresApi(Build.VERSION_CODES.O)
fun startLocationUpdates(
    bus_no: String,
    fusedLocationClient: FusedLocationProviderClient,
    context: Context,
    onLocationFetched: (LatLng) -> Unit,
    geofenceCenters: List<LatLng>,
    checkPoints: List<LatLng>,
    geofenceRadius: Double
) {
    val db = Firebase.firestore
    val date_ = getCurrentDate()
    db.collection("buses")
        .document(bus_no)
        .get()
        .addOnSuccessListener { busDetails ->
            val checkpointsMap = busDetails.get("check_points") as? Map<String, Any> ?: emptyMap()
            val checkpointKeys = checkpointsMap.keys.toList()
            Log.d("Check Point Keys: ", checkpointKeys.toString())
            val firstCheckpoint = checkpointKeys.firstOrNull() ?: ""
            Log.d("UpcomingCheckpoint", "Next checkpoint assigned: $firstCheckpoint")
            if (!busDetails.contains(date_)) {
                db.collection("bus_status")
                    .document(bus_no)
                    .set(mapOf(
                        date_ to mapOf(
                            "count" to 0,
                            "bus_operable" to true,
                            "upcoming_checkpoint" to firstCheckpoint,
                            "rescue_request" to false
                    )), SetOptions.merge())
                    .addOnSuccessListener {
                        Log.d("Firestore", "Date structure for $date_ ensured.")
                    }
                    .addOnFailureListener {
                        Log.e("Firestore", "Failed to ensure date structure for $date_.", it)
                    }
            }
        }
        .addOnFailureListener {
            Log.e("Firestore", "Failed to fetch document for $bus_no.", it)
        }

    if (ActivityCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    ) {
        val locationRequest = LocationRequest.create().apply {
            interval = 5000 // 5 seconds
            fastestInterval = 5000 // 5 seconds
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        val database = FirebaseDatabase.getInstance("https://vbus-160e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
        val databaseRef = database.getReference("driverLocation").child(bus_no)

        // Maintain a map to track the previous state of each geofence and check_point
        val previousStateMap = mutableMapOf<Int, Boolean>().apply {
            geofenceCenters.indices.forEach { this[it] = false }
            checkPoints.indices.forEach { this[it + geofenceCenters.size] = false }
        }

        fusedLocationClient.requestLocationUpdates(locationRequest, object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                locationResult.lastLocation?.let { location ->
                    val latLng = LatLng(location.latitude, location.longitude)
                    // Update location in Firebase Realtime Database
                    val locationData = mapOf(
                        "latitude" to location.latitude,
                        "longitude" to location.longitude,
                        "timestamp" to System.currentTimeMillis()
                    )
                    databaseRef.setValue(locationData)

                    // Call the callback function to notify about the location change
                    onLocationFetched(latLng)

                    // Check geofence and check points status
                    geofenceCenters.forEachIndexed { index, center ->
                        val isInside = checkGeofence(latLng, center, geofenceRadius)
                        val previousState = previousStateMap[index] ?: false

                        if (!previousState && isInside) {
                            Log.d("GeofenceEvent", "Bus entered geofence ${index + 1}.")
                            val timestamp = System.currentTimeMillis()

                            // Update Firestore under bus_no -> date -> s{index + 1}
                            val busRef = db.collection("bus_status").document(bus_no)

                            // First, update the timestamp (ensuring "s${index + 1}" is set)
                            busRef.set(
                                mapOf(
                                    date_ to mapOf("s${index + 1}" to timestamp)
                                ),
                                SetOptions.merge() // Ensures existing fields remain
                            ).addOnSuccessListener {
                                Log.d("Firestore", "Timestamp for geofence ${index + 1} under date $date_ updated.")
                            }

                            // Then, increment the "count" field
                            busRef.update("$date_.count", FieldValue.increment(20))
                                .addOnSuccessListener {
                                    Log.d("Firestore", "Count incremented by 20 under date $date_.")
                                }
                                .addOnFailureListener { e ->
                                    Log.e("Firestore", "Error updating count: ", e)
                                }

                        }
                        previousStateMap[index] = isInside
                    }

                    checkPoints.forEachIndexed { index, point ->
                        val isInside = checkGeofence(latLng, point, geofenceRadius)
                        val previousState = previousStateMap[geofenceCenters.size + index] ?: false

                        if (!previousState && isInside) {
                            Log.d("CheckpointEvent", "Bus $bus_no entered checkpoint ${index + 1}.")
                            val checkpointValue = "c${index + 1}" // Example: "c1", "c2"
                            val busRef = db.collection("bus_status").document(bus_no)

                            busRef.get().addOnSuccessListener { document ->
                                val existingData = document.get(date_) as? MutableMap<String, Any> ?: mutableMapOf()
                                val checkpoints = (existingData["checkpoints"] as? MutableList<String>) ?: mutableListOf()

                                if (!checkpoints.contains(checkpointValue)) {
                                    checkpoints.add(checkpointValue)
                                    existingData["checkpoints"] = checkpoints

                                    // ✅ Update upcoming checkpoint
                                    val nextCheckpointValue = if (index + 1 < checkPoints.size) {
                                        "c${index + 2}" // Next checkpoint
                                    } else {
                                        "End of Route" // No further checkpoints
                                    }
                                    existingData["upcoming_checkpoint"] = nextCheckpointValue

                                    busRef.update(date_, existingData)
                                }
                            }
                        }

                        previousStateMap[geofenceCenters.size + index] = isInside
                    }


                    //logic for rescue request
                    db.collection("bus_status").document(bus_no)

                } ?: Log.e("LocationResult", "Location is null")
            }
        }, Looper.getMainLooper())

    }
    else {
        Log.e("Permission", "Location permission not granted")
    }
}


fun checkGeofence(location: LatLng, geofenceCenter: LatLng, radius: Double): Boolean {
    val distance = haversineDistance(location, geofenceCenter)
    return distance <= radius
}

fun haversineDistance(loc1: LatLng, loc2: LatLng): Double {
    val RADIUS_EARTH_METERS = 6371000.0 // Earth's radius in meters
    val latDiff = Math.toRadians(loc2.latitude - loc1.latitude)
    val lonDiff = Math.toRadians(loc2.longitude - loc1.longitude)
    val a = sin(latDiff / 2).pow(2.0) + cos(Math.toRadians(loc1.latitude)) *
            cos(Math.toRadians(loc2.latitude)) * sin(lonDiff / 2).pow(2.0)
    return 2 * RADIUS_EARTH_METERS * asin(sqrt(a))
}

@RequiresApi(Build.VERSION_CODES.O)
fun getCurrentDate(): String {
    val currentDate = LocalDate.now()
    val formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")
    return currentDate.format(formatter)
}

@RequiresApi(Build.VERSION_CODES.O)
fun alertNearbyBuses(brokenBusNo: String) {
    val db = Firebase.firestore
    val date_ = getCurrentDate()

    // Step 0.0: Update isOperable to false for the broken bus
    db.collection("bus_status")
        .document(brokenBusNo)
        .update("$date_.bus_operable", false)

    // Step 0.1: Store broken bus lat and long
    val database = FirebaseDatabase.getInstance("https://vbus-160e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val busRef = database.getReference("driverLocation").child(brokenBusNo)

    busRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val latitude = snapshot.child("latitude").getValue(Double::class.java)
            val longitude = snapshot.child("longitude").getValue(Double::class.java)

            if (latitude == null || longitude == null) {
                Log.e("Firebase", "Bus $brokenBusNo location data is missing!")
                return
            }

            Log.d("Firebase", "Bus $brokenBusNo Location: Lat = $latitude, Long = $longitude")

            // Step 1: Fetch route_id of the broken bus
            db.collection("buses").document(brokenBusNo)
                .get()
                .addOnSuccessListener { brokenBusDetails ->
                    val routeId = brokenBusDetails.getString("route_id") ?: return@addOnSuccessListener

                    // Step 2: Identify active buses on the same route
                    val activeBusesInRoute = routes[routeId] ?: emptyList()
                    Log.d("Active Buses", activeBusesInRoute.toString())

                    // Step 3: Identify upcoming checkpoint and location of the broken bus
                    db.collection("bus_status").document(brokenBusNo)
                        .get()
                        .addOnSuccessListener { busData ->
                            val brokenBusData = busData.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
                            val upcomingCheckpoint = brokenBusData["upcoming_checkpoint"] as? String
                            Log.d("Upcoming Checkpoint", upcomingCheckpoint.toString())

                            for (activeBusNo in activeBusesInRoute) {
                                if (activeBusNo == brokenBusNo) continue // Skip itself

                                // Step 4: Check if the active bus hasn’t crossed the upcoming checkpoint
                                db.collection("bus_status").document(activeBusNo)
                                    .get()
                                    .addOnSuccessListener { thisBus ->
                                        val thisBusData = thisBus.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
                                        val checkPointsMap = thisBusData["check_points"] as? Map<String, Any> ?: emptyMap()
                                        val checkPointNames = checkPointsMap.keys.toList()

                                        if (!checkPointNames.contains(upcomingCheckpoint)) {
                                            Log.d("Alert",
                                                "Notify bus $activeBusNo about the broken bus $brokenBusNo")

                                            // Step 5: Mark active bus for rescue
                                            db.collection("bus_status").document(activeBusNo)
                                                .set(
                                                    mapOf(
                                                        date_ to mapOf(
                                                            "rescue_request" to true,
                                                            "rescue_location" to mapOf(
                                                                "latitude" to latitude,
                                                                "longitude" to longitude
                                                            )
                                                        )
                                                    ),
                                                    SetOptions.merge()
                                                )
                                        }
                                    }
                            }
                        }
                }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read data", error.toException())
        }
    })
}
