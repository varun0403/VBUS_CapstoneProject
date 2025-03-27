package com.example.vbus.student

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.Text
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
fun TrackComplaints(navController: NavController, name: String) {
    val db = FirebaseFirestore.getInstance()
    var complaints by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Fetch complaints from Firestore
    LaunchedEffect(Unit) {
        val complaintList = mutableListOf<Map<String, Any>>()
        try {
            val querySnapshot = db.collection("complaints")
                .whereEqualTo("email", name)
                .get()
                .await()

            for (document in querySnapshot.documents) {
                document.data?.let {
                    complaintList.add(it)
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        complaints = complaintList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Header
        Text(
            text = "Track Your Complaints",
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            style = androidx.compose.material3.MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            color = Color(0xFF6200EE) // Purple theme color
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Display each complaint as an elevated card
        complaints.forEach { complaint ->
            var isExpanded by remember { mutableStateOf(false) }
            val status = complaint["status"] as? String ?: "unknown"
            val reply = (complaint["reply"] as? Map<*, *>)?.get("reply_msg") as? String ?: "No reply yet"

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp)
                    .clickable { isExpanded = !isExpanded },
//                elevation = 4.dp,
                shape = RoundedCornerShape(12.dp)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        // Subject
                        Text(
                            text = complaint["subject"] as? String ?: "No Subject",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyLarge,
                            modifier = Modifier.weight(1f),
                            color = Color.Black
                        )
                        // Status Chip
                        Box(
                            modifier = Modifier
                                .background(
                                    color = when (status.lowercase()) {
                                        "solved" -> Color(0xFF4CAF50) // Green
                                        "open", "getting resolved" -> Color(0xFFFFC107) // Yellow
                                        else -> Color(0xFF9E9E9E) // Gray
                                    },
                                    shape = RoundedCornerShape(16.dp)
                                )
                                .padding(horizontal = 12.dp, vertical = 4.dp)
                        ) {
                            Text(
                                text = status.capitalize(),
                                style = androidx.compose.material3.MaterialTheme.typography.bodySmall,
                                color = Color.White
                            )
                        }
                    }

                    if (isExpanded) {
                        Spacer(modifier = Modifier.height(8.dp))
                        // Status
                        Text(
                            text = "Status: $status",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        // Reply
                        Text(
                            text = "Reply: $reply",
                            style = androidx.compose.material3.MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }
                }
            }
        }
    }
}
