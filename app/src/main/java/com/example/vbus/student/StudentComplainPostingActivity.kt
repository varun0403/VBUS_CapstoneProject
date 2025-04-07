package com.example.vbus.student

import android.util.Log
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.ui.platform.LocalContext
import com.example.vbus.notifications.MyFirebaseMessagingService

@Composable
fun PostComplaint(navController: NavController, name: String) {
    val context = LocalContext.current
    var subject by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var showDialog = remember { mutableStateOf(false) }
    val db = FirebaseFirestore.getInstance()
    var ref_id by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        verticalArrangement = Arrangement.Center, horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OutlinedTextField(
            value = name,
            onValueChange = {},
            label = { Text("Name") },
            enabled = false
        )
        OutlinedTextField(
            value = subject,
            onValueChange = { subject = it },
            label = { Text("Subject") }
        )
        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description") }
        )
        Button(
            onClick = {
                // Create the complaint data map
                val complaint = mapOf(
                    "email" to name,
                    "subject" to subject,
                    "description" to description,
                    "status" to "open",
                    "timestamp" to System.currentTimeMillis(),
                    "reply" to mapOf(
                        "reply_msg" to " ",
                        "reply_timestamp" to " "
                    )
                )
                // Insert the data into Firestore
                db.collection("complaints")
                    .add(complaint)  // Automatically generates a unique document ID
                    .addOnSuccessListener { documentReference ->
                        ref_id = documentReference.id
                        showDialog.value = true
                    }
                    .addOnFailureListener { e ->
                        Log.w("Firestore", "Error adding document", e)
                    }
            },
            enabled = (subject.isNotBlank() && description.isNotBlank())
        ) { Text(text = "Submit") }

        if (showDialog.value) {
            AlertDialog(
                onDismissRequest = { showDialog.value = false },
                title = { Text(text = "Success") },
                text = { Text(text = "Ticket ID: $ref_id") },
                confirmButton = {
                    Button(onClick = {
                        showDialog.value = false
                        MyFirebaseMessagingService
                            .showNotification(
                                context, "Complaint Registered",
                                "Reference ID: $ref_id")
                        navController.navigate("student_home/$name")
                    }) {
                        Text("OK")
                    }
                }
            )
        }
    }
}
