package com.example.vbus.admin

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items

@Composable
fun AdminComplaintResolved(navController: NavController) {
    val db = FirebaseFirestore.getInstance()
    var closedComplaints by remember { mutableStateOf<List<Map<String, Any>>>(emptyList()) }

    // Fetch complaints with status "solved"
    LaunchedEffect(Unit) {
        val complaintList = mutableListOf<Map<String, Any>>()
        try {
            val querySnapshot = db.collection("complaints")
                .whereEqualTo("status", "solved")
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
        closedComplaints = complaintList
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text(
            text = "Resolved Complaints",
            modifier = Modifier.fillMaxWidth(),
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center
        )
        Spacer(modifier = Modifier.height(16.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize()
        ) {
            items(closedComplaints) { complaint ->
                ComplaintCard(complaint)
            }
        }
    }
}

@Composable
fun ComplaintCard(complaint: Map<String, Any>) {
    var isExpanded by remember { mutableStateOf(false) }
    val subject = complaint["subject"] as? String ?: "No Subject"
    val description = complaint["description"] as? String ?: "No Description"
    val email = complaint["email"] as? String ?: "No Email"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFFE8F5E9))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Resolved",
                    tint = Color(0xFF4CAF50),
                    modifier = Modifier.size(24.dp)
                )

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = subject,
                    style = MaterialTheme.typography.titleMedium,
                    color = Color.Black,
                    modifier = Modifier.weight(1f)
                )

                IconButton(onClick = { isExpanded = !isExpanded }) {
                    Icon(
                        imageVector = if (isExpanded) Icons.Default.KeyboardArrowUp else Icons.Default.KeyboardArrowDown,
                        contentDescription = if (isExpanded) "Collapse" else "Expand",
                        tint = Color.Black
                    )
                }
            }

            if (isExpanded) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "Description: ",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )

                Text(
                    text = "Email:",
                    style = MaterialTheme.typography.bodyMedium,
                    color = Color.Gray
                )
                Text(
                    text = email,
                    style = MaterialTheme.typography.bodyLarge,
                    color = Color.Black,
                    modifier = Modifier.padding(vertical = 4.dp)
                )
            }
        }
    }
}
