package com.example.vbus.driver

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Looper
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.core.app.ActivityCompat
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.vbus.routes
import com.example.vbus.studentBoardingStatus
import com.example.vbus.student_stops
import com.example.vbus.bus_checkpoints
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.*
import com.google.firebase.firestore.GeoPoint
import com.example.vbus.checkpointToBuses
import com.example.vbus.notification.NotificationBody
import com.example.vbus.notification.NotificationMessage
import com.example.vbus.notification.NotificationWrapper
import com.example.vbus.notification.sendingNotification

@RequiresApi(Build.VERSION_CODES.O)
@Composable
fun DriverMapScreen(navController: NavHostController, bus_no: String) {
    val context = LocalContext.current
    val fusedLocationClient = remember { LocationServices.getFusedLocationProviderClient(context) }
    val db = Firebase.firestore
    val fcmToken = "e0Zqi72kRHqoFsO2jox6GR:APA91bHmDCCt8xYcguqkYlvJd4nPzUE11UnW6yN3ktEbFbMWv1w6Xtdt7qYGoc9NmRGoPl2PK2IkwMSGcij1wOVpb0hySsfzaLJ_7tQtdfQmUjIAhjjhV7I"
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
        val documentRef = db.collection("buses").document(bus_no)
        Log.d("Bus no",bus_no)
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
                            }
                            else {
                                Log.d("FirestoreData", "Invalid GeoPoint for key=$key: $value")
                            }
                        }
                        Log.d("FirestoreData", "Final geofence centers: $geofenceCenters")
                    }
                    else {
                        Log.d("FirestoreData", "No 'stops' field found in document.")
                    }
                }
                else {
                    Log.d("FirestoreData", "Document does not exist")
                }
            }
            .addOnFailureListener { exception ->
                Log.e("FirestoreData", "Error fetching document", exception)
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
        }
        else {
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
                Row {
                    Button(
                        onClick = {
                            alertNearbyBuses(bus_no)
                            val notificationBody = NotificationBody(
                                title = "Authentication",
                                body = "You have logged in successfully!"
                            )
                            val message = NotificationMessage(
                                token = fcmToken,
                                notification = notificationBody
                            )
                            val wrapper = NotificationWrapper(message = message)
                            sendingNotification(wrapper, context)
                        }
                    ) { Text("Report Breakdown") }

                    Button(
                        onClick = {
                            navController.navigate("driver_rescue_map/$bus_no")
                        },
                    ) { Text("Navigate to broken bus") }
                }
            }
        }
    }
}


@Composable
fun LoadingScreen() {
    CircularProgressIndicator()
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
            val route_id = busDetails.getString("route_id") ?: ""
            val stops = busDetails.get("stops") as? Map<String, Any> ?: emptyMap()
            val stopsKeys = stops.keys.toList()
            Log.d("Check Point Keys: ", checkpointKeys.toString())
            val firstCheckpoint = checkpointKeys.firstOrNull() ?: ""
            Log.d("UpcomingCheckpoint", "Next checkpoint assigned: $firstCheckpoint")
            if (!busDetails.contains(date_)) {
                db.collection("bus_status").document(bus_no).set(mapOf(
                    date_ to mapOf(
                        "count" to 0,
                        "bus_operable" to true,
                        "upcoming_checkpoints" to bus_checkpoints[route_id],
                        "rescue_request" to false,
                        "upcoming_stops" to stopsKeys,
                        "boarding_status" to studentBoardingStatus[bus_no])),
                    SetOptions.merge())
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

    if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION)
        == PackageManager.PERMISSION_GRANTED) {
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
                            val busRef = db.collection("bus_status").document(bus_no)

                            busRef.get().addOnSuccessListener { document ->
                                val existingData = document.get(date_) as? MutableMap<String, Any> ?: mutableMapOf()
                                val timestamps = (existingData["timestamp"] as? MutableMap<String, Long>) ?: mutableMapOf()
                                val stops = (existingData["upcoming_stops"] as? MutableList<String>) ?: mutableListOf()
                                stops.remove("s${index + 1}")
                                existingData["upcoming_stops"] = stops
                                // Save current stop timestamp
                                timestamps["s${index + 1}"] = timestamp
                                existingData["timestamp"] = timestamps

                                // ✅ Set upcoming stop
                                val nextStopValue = if (index + 1 < geofenceCenters.size) {
                                    "s${index + 2}"
                                }
                                else {
                                    "VIT"
                                }
                                existingData["upcoming_stop"] = nextStopValue
                                busRef.set(
                                    mapOf(date_ to existingData
                                    ), SetOptions.merge())
                                    .addOnSuccessListener {
                                        Log.d("Firestore", "Stop ${index + 1} timestamp and upcoming_stop updated for date $date_.")
                                    }
                            }
                        }
                        previousStateMap[index] = isInside
                    }

                    checkPoints.forEachIndexed { index, point ->
                        val isInside = checkGeofence(latLng, point, geofenceRadius)
                        val previousState = previousStateMap[geofenceCenters.size + index] ?: false

                        if (!previousState && isInside) {
                            Log.d("CheckpointEvent", "Bus $bus_no entered a checkpoint")
                            val busRef = db.collection("bus_status").document(bus_no)

                            busRef.get().addOnSuccessListener { document ->
                                val existingData = document.get(date_) as? MutableMap<String, Any> ?: mutableMapOf()
                                val upcomingCheckpoints = (existingData["upcoming_checkpoints"] as? MutableList<String>) ?: mutableListOf()
                                val checkPointToRemove = upcomingCheckpoints.firstOrNull()
                                if (checkPointToRemove != null) {
                                    upcomingCheckpoints.remove(checkPointToRemove)
                                }
                                existingData["upcoming_checkpoints"] = upcomingCheckpoints
                                busRef.update(date_, existingData)
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
    val fcmToken = "e0Zqi72kRHqoFsO2jox6GR:APA91bHmDCCt8xYcguqkYlvJd4nPzUE11UnW6yN3ktEbFbMWv1w6Xtdt7qYGoc9NmRGoPl2PK2IkwMSGcij1wOVpb0hySsfzaLJ_7tQtdfQmUjIAhjjhV7I"

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
                            val upcomingCheckpointsBrokenBus = brokenBusData["upcoming_checkpoints"] as? List<String> ?: emptyList()
                            val upcomingCheckpointBrokenBus = upcomingCheckpointsBrokenBus.firstOrNull()
                            Log.d("Upcoming Checkpoints", upcomingCheckpointsBrokenBus.toString())
                            val validBusesForRescue = mutableMapOf<String,Int>()
                            for (activeBusNo in activeBusesInRoute) {
                                if (activeBusNo == brokenBusNo) continue // Skip itself

                                // Step 4: Check if the active bus hasn’t crossed the upcoming checkpoint
                                db.collection("bus_status").document(activeBusNo)
                                    .get()
                                    .addOnSuccessListener { thisBus ->
                                        val thisBusData = thisBus.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
                                        val upcomingCheckpointsActiveBus = thisBusData["upcoming_checkpoints"] as? List<String> ?: emptyList()
                                        val rescue_req = thisBusData["rescue_request"] as? Boolean ?: false
                                        if (upcomingCheckpointsActiveBus.contains(upcomingCheckpointBrokenBus) && rescue_req) {
                                            // Step 4.1 logic: check how many pending stops left for the active bus
                                            val upcomingStops = thisBusData["upcoming_stops"] as? List<String> ?: emptyList()
                                            val num_of_upcomingStops = upcomingStops.size
                                            if (num_of_upcomingStops == 0){
                                                val available_seats = 20 - (thisBusData["count"] as? Long ?: 0).toInt()
                                                if(available_seats > 0){
                                                    validBusesForRescue[activeBusNo] = available_seats
                                                    Log.d("Alert", "Notify bus $activeBusNo about the broken bus $brokenBusNo. Available seats $available_seats")
                                                }
                                            }
                                            else{
                                                var pendingBoardingCount = 0
                                                // Fetch map of stop-wise students for this active bus
                                                val stopWiseStudents = student_stops[activeBusNo] ?: mutableMapOf()
                                                // Loop over remaining upcoming stops and count students at each
                                                for (stop in upcomingStops) {
                                                    val studentsAtStop = stopWiseStudents[stop] ?: mutableListOf()
                                                    pendingBoardingCount += studentsAtStop.size
                                                }
                                                val currentCount = (thisBusData["count"] as? Long ?: 0).toInt()
                                                val availableSeats = 20 - currentCount - pendingBoardingCount
                                                Log.d("Seat Calculation", "Bus $activeBusNo has $availableSeats available after accounting for $pendingBoardingCount pending boarders.")
                                                if (availableSeats > 0) {
                                                    validBusesForRescue[activeBusNo] = availableSeats
                                                    Log.d("Alert", "Notify bus $activeBusNo about the broken bus $brokenBusNo. Available seats $availableSeats")
                                                }
                                            }
                                            // Step 5.2: Mark active bus for rescue
                                            db.collection("bus_status").document(activeBusNo)
                                                .set(
                                                    mapOf(
                                                        date_ to mapOf(
                                                            "rescue_request" to true,
                                                            "rescue_location" to GeoPoint(latitude, longitude),
                                                            "rescue_bus" to brokenBusNo
                                                        )
                                                    ),
                                                    SetOptions.merge()
                                                )
                                        }
                                    }
                            }

                            fetchBoardedStudents(brokenBusNo, date_) { boardedStudents ->
                                allocateStudentsToBuses(boardedStudents, validBusesForRescue, date_, brokenBusNo)
                            }
                        }
                }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("Firebase", "Failed to read data", error.toException())
        }
    })
}

fun fetchBoardedStudents(brokenBusNo: String, date_: String, callback: (List<String>) -> Unit) {
    val db = Firebase.firestore
    db.collection("bus_status").document(brokenBusNo)
        .get()
        .addOnSuccessListener { busData ->
            val brokenBusData = busData.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
            val studentsMap = brokenBusData["boarding_status"] as? Map<String, Long> ?: return@addOnSuccessListener
            val boardedStudents = studentsMap.filterValues { it == 1L }.keys.toList()
            val waitingStudents = mutableListOf<String>()
            val upcomingStopsBrokenBus = brokenBusData["upcoming_stops"] as? List<String> ?: emptyList()
            val num_of_upcomingStopsBrokenBus = upcomingStopsBrokenBus.size
            if (num_of_upcomingStopsBrokenBus > 0){
                var pendingBoardingCountBrokenBus = 0
                // Fetch map of stop-wise students for this active bus
                val stopWiseStudents = student_stops[brokenBusNo] ?: mutableMapOf()
                // Loop over remaining upcoming stops and count students at each
                for (stop in upcomingStopsBrokenBus) {
                    val studentsAtStop = stopWiseStudents[stop] ?: mutableListOf()
                    pendingBoardingCountBrokenBus += studentsAtStop.size
                    for (students in studentsAtStop){
                        waitingStudents.add(students)
                    }
                }
                val currentCount = (brokenBusData["count"] as? Long ?: 0).toInt()
                val totalStudentRescueCount = currentCount + pendingBoardingCountBrokenBus
            }
            val allStudents = boardedStudents + waitingStudents
            callback(allStudents)// Pass the boarded students list to next step
        }
        .addOnFailureListener {
            Log.e("Firestore", "Failed to fetch boarded students", it)
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun allocateStudentsToBuses(
    rescueStudents: List<String>,
    seatAvailability: Map<String, Int>,
    date_: String,
    brokenBusNo: String
) {
    val db = Firebase.firestore
    val allocatedBuses = mutableMapOf<String, String>()
    val rescue_buses = seatAvailability.keys.toList()
    var studentIndex = 0
    for ((busNo, seats) in seatAvailability) {
        for (i in 0 until seats) {
            if (studentIndex >= rescueStudents.size) {
                break
            }
            allocatedBuses[rescueStudents[studentIndex]] = busNo
            studentIndex++
        }
    }

    // Remaining students with no available seat
//    while (studentIndex < rescueStudents.size) {
//        // needs to be set to second lvl of rescue
//        allocatedBuses[rescueStudents[studentIndex]] = "NA"
//        studentIndex++
//    }

    if (studentIndex < rescueStudents.size) {
        val unallocatedStudents = rescueStudents.subList(studentIndex, rescueStudents.size)
        val allocatedBusList = allocatedBuses.values.toList()

        rescueLayer2(
            unallocatedStudents = unallocatedStudents,
            allocatedBuses = allocatedBusList,
            brokenBusNo = brokenBusNo
        )
    }


    // Update the allocated buses in Firestore under the broken bus entry
    db.collection("bus_status").document(brokenBusNo)
        .get()
        .addOnSuccessListener { thisBus ->
            val thisBusData = thisBus.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
            val updateData = mapOf(
                date_ to mapOf(
                    "allocated_buses" to allocatedBuses
                )
            )
            db.collection("bus_status").document(brokenBusNo)
                .set(updateData, SetOptions.merge())
                .addOnSuccessListener {
                    Log.d("Firestore", "Successfully updated allocated_buses for $brokenBusNo under $date_")
                }
                .addOnFailureListener {
                    Log.e("Firestore", "Failed to update allocated_buses for $brokenBusNo under $date_", it)
                }
        }
}

@RequiresApi(Build.VERSION_CODES.O)
fun rescueLayer2(
    unallocatedStudents: List<String>,
    allocatedBuses: List<String>,
    brokenBusNo: String
) {
    val db = Firebase.firestore
    val layer2CandidateBuses = mutableSetOf<String>()
    val layer2EligibleBuses = mutableMapOf<String, Int>()
    var completedCount = 0
    val totalToCheck: Int
    var brokenBusCheckpoints = emptyList<String>()
    val date_ = getCurrentDate()

    db.collection("bus_status").document(brokenBusNo)
        .get()
        .addOnSuccessListener { busData ->
            val brokenBusData = busData.get(date_) as? Map<*, *> ?: return@addOnSuccessListener
            brokenBusCheckpoints = brokenBusData["upcoming_checkpoints"] as? List<String> ?: emptyList()
        }

    for (checkpoint in brokenBusCheckpoints) {
        val busesAtCheckpoint = checkpointToBuses[checkpoint] ?: continue
        for (bus in busesAtCheckpoint) {
            if (bus != brokenBusNo && bus !in allocatedBuses) {
                layer2CandidateBuses.add(bus)
            }
        }
    }

    totalToCheck = layer2CandidateBuses.size

    if (totalToCheck == 0) {
        Log.d("Layer2", "No eligible buses found at common checkpoints.")
        return
    }

    for (bus in layer2CandidateBuses) {
        db.collection("bus_status").document(bus).get()
            .addOnSuccessListener { doc ->
                val busData = doc.get(getCurrentDate()) as? Map<*, *> ?: return@addOnSuccessListener
                val rescue_req = busData["rescue_request"] as? Boolean ?: return@addOnSuccessListener

                if (!rescue_req) {
                    val activeBusCheckPoints = busData["upcoming_checkpoints"] as? List<*> ?: return@addOnSuccessListener
                    val brokenBusUpcomingCheckpoint = brokenBusCheckpoints.firstOrNull()
                    if (activeBusCheckPoints.contains(brokenBusUpcomingCheckpoint)) {
                        val upcomingStops = busData["upcoming_stops"] as? List<String> ?: emptyList()
                        val currentCount = (busData["count"] as? Long ?: 0).toInt()

                        val availableSeats = if (upcomingStops.isEmpty()) {
                            20 - currentCount
                        } else {
                            var pendingBoardingCount = 0
                            val stopWiseStudents = student_stops[bus] ?: mutableMapOf()
                            for (stop in upcomingStops) {
                                val studentsAtStop = stopWiseStudents[stop] ?: mutableListOf()
                                pendingBoardingCount += studentsAtStop.size
                            }
                            20 - currentCount - pendingBoardingCount
                        }

                        if (availableSeats > 0) {
                            layer2EligibleBuses[bus] = availableSeats
                            Log.d("Alert", "Notify bus $bus about broken bus $brokenBusNo. Available seats: $availableSeats")
                        }
                    }
                }
                else {
                    Log.d("Status", "Bus $bus is already on another rescue.")
                }

                completedCount++
                if (completedCount == totalToCheck) {
                    allocateFromLayer2(unallocatedStudents, layer2EligibleBuses, brokenBusNo)
                }
            }
            .addOnFailureListener {
                completedCount++
                if (completedCount == totalToCheck) {
                    allocateFromLayer2(unallocatedStudents, layer2EligibleBuses, brokenBusNo)
                }
            }
    }
}


@RequiresApi(Build.VERSION_CODES.O)
fun allocateFromLayer2(
    unallocatedStudents: List<String>,
    eligibleBuses: Map<String, Int>,
    brokenBusNo: String
) {

    val db = Firebase.firestore
    val allocatedBuses2 = mutableMapOf<String, String>()
    var studentIndex = 0
    val database = FirebaseDatabase.getInstance("https://vbus-160e8-default-rtdb.asia-southeast1.firebasedatabase.app/")
    val busRef = database.getReference("driverLocation").child(brokenBusNo)
    var location = GeoPoint(0.0, 0.0)
    busRef.addListenerForSingleValueEvent(object : ValueEventListener {
        override fun onDataChange(snapshot: DataSnapshot) {
            val lat = snapshot.child("latitude").getValue(Double::class.java)
            val lng = snapshot.child("longitude").getValue(Double::class.java)

            if (lat != null && lng != null) {
                location = GeoPoint(lat,lng)
                Log.d("DriverLocation", "Bus $brokenBusNo is at: $location")
            } else {
                Log.e("DriverLocation", "Failed to retrieve location for $brokenBusNo")
            }
        }

        override fun onCancelled(error: DatabaseError) {
            Log.e("DriverLocation", "Database error: ${error.message}")
        }
    })

    for ((busNo, seats) in eligibleBuses) {
        for (i in 0 until seats) {
            if (studentIndex >= unallocatedStudents.size) break
            allocatedBuses2[unallocatedStudents[studentIndex]] = busNo
            studentIndex++
        }
    }

    while (studentIndex < unallocatedStudents.size) {
        allocatedBuses2[unallocatedStudents[studentIndex]] = "NA"
        studentIndex++
    }

    val updateData = mapOf(
        getCurrentDate() to mapOf(
            "layer2_allocated_buses" to allocatedBuses2,
            "rescue_location" to location,
            "rescue_bus" to brokenBusNo,
        )
    )

    db.collection("bus_status").document(brokenBusNo)
        .set(
            updateData,
            SetOptions.merge()
        )
        .addOnSuccessListener {
            Log.d("Firestore", "Layer 2 allocations updated for $brokenBusNo.")
        }
        .addOnFailureListener {
            Log.e("Firestore", "Failed to update layer 2 allocations for $brokenBusNo", it)
        }
}


