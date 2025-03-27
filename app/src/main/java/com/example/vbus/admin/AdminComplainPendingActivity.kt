package com.example.vbus.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun AdminComplaintPending(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var openComplaints by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }
    var replyText by remember { mutableStateOf("") }

    // Fetch complaints with status "open"
    LaunchedEffect(Unit) {
        val complaintList = mutableListOf<Map<String, Any>>()
        try {
            val querySnapshot = db.collection("complaints")
                .whereEqualTo("status", "open")
                .get()
                .await()

            for (document in querySnapshot.documents) {
                val data = document.data?.toMutableMap()
                data?.set("id", document.id) // Store document ID for updates
                data?.let { complaintList.add(it) }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        openComplaints = complaintList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Admin Complaint Due Section",
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleLarge,
            color = Color(0xFF6200EE) // Purple color for the header
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display each complaint
        openComplaints.forEach { complaint ->
            var isExpanded by remember { mutableStateOf(false) }
            val complaintId = complaint["id"] as? String ?: ""
            val subject = complaint["subject"] as? String ?: "No Subject"
            val description = complaint["description"] as? String ?: "No Description"
            val email = complaint["email"] as? String ?: "No Email"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { isExpanded = !isExpanded },
                shape = RoundedCornerShape(12.dp),
                elevation = CardDefaults.cardElevation(4.dp)
            ) {
                Column(
                    modifier = Modifier
                        .background(Color(0xFFF9FBE7))
                        .padding(16.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = subject,
                            style = MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = Color.Black
                        )
                        Box(
                            modifier = Modifier
                                .background(
                                    color = Color(0xFFFFC107), // Yellow for pending
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = "Pending",
                                style = MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))

                        // Complaint Details
                        Text(
                            text = "Description: $description",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Email: $email",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Reply Input
                        OutlinedTextField(
                            value = replyText,
                            onValueChange = { replyText = it },
                            label = { Text("Reply to the student") },
                            modifier = Modifier.fillMaxWidth()
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        // Submit Button
                        Button(
                            onClick = {
                                // Update Firestore with reply and mark as resolved
                                db.collection("complaints")
                                    .document(complaintId)
                                    .update(
                                        mapOf(
                                            "reply.reply_msg" to replyText,
                                            "reply.reply_timestamp" to System.currentTimeMillis(),
                                            "status" to "solved"
                                        )
                                    )
                                    .addOnSuccessListener {
                                        // Refresh the list by removing the resolved complaint
                                        openComplaints = openComplaints.filter { it["id"] != complaintId }
                                    }
                                    .addOnFailureListener { e ->
                                        e.printStackTrace()
                                    }
                            },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            Text("Submit Reply")
                        }
                    }
                }
            }
        }
    }
}