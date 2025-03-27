package com.example.vbus.admin

import androidx.compose.foundation.layout.*
import androidx.compose.material.MaterialTheme
import androidx.compose.material.MaterialTheme.typography
import androidx.compose.material.OutlinedTextField
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.tasks.await

@Composable
fun FetchStudentDataScreen() {
    val db = FirebaseFirestore.getInstance()
    var email by remember { mutableStateOf("") }
    var studentData by remember { mutableStateOf<Map<String, Any>?>(null) }
    var errorMessage by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Fetch Student Data",
            style = typography.h4,
            modifier = Modifier.fillMaxWidth(),
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(16.dp))

        // TextField for Registration Number
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter email id") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter date") },
            modifier = Modifier.fillMaxWidth(),
            enabled = false
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Button to Fetch Data
        Button(
            onClick = {
                if (email.isNotEmpty()) {
                    fetchStudentData(db, email) { data, error ->
                        if (data != null) {
                            studentData = data
                            errorMessage = ""
                        }
                        else {
                            errorMessage = error ?: "Unknown error occurred"
                        }
                    }
                }
                 else {
                    errorMessage = "Please enter a registration number"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Fetch Data")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display Student Data
        studentData?.let { data ->
            Text("Name: ${data["student_name"]}", style = typography.h4)
            Text("Reg No: ${data["reg_no"]}", style = typography.h4)
            Text("Bus No: ${data["bus_no"]}", style = typography.h4)
        }

        // Display Error Message
        if (errorMessage.isNotEmpty()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = errorMessage,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

fun fetchStudentData(
    db: FirebaseFirestore,
    regNo: String,
    callback: (data: Map<String, Any>?, error: String?) -> Unit
) {
    db.collection("students")
        .document(regNo)
        .get()
        .addOnSuccessListener { document ->
            if (document.exists()) {
                callback(document.data, null)
            } else {
                callback(null, "Student not found")
            }
        }
        .addOnFailureListener { exception ->
            callback(null, exception.localizedMessage)
        }
}
