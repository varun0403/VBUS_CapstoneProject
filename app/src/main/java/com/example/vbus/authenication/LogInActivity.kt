package com.example.vbus.authenication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.snapping.SnapPosition
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import androidx.compose.ui.text.input.PasswordVisualTransformation
import com.example.vbus.student.StudentHomeScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import android.util.Log
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

@Composable
fun LoginScreen(navController: NavController) {
    var email_val by remember { mutableStateOf("") }
    var password_val by remember { mutableStateOf("") }
    var userType by remember { mutableStateOf(0) } //yet to be implemented
    val auth: FirebaseAuth = Firebase.auth
    var errorMessage by remember { mutableStateOf<String?>(null) }
    // 1->student/faculty, 2->admin, 3->driver, 4->parent

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),verticalArrangement = Arrangement.Center){
        Text(text = "Login Screen")
        OutlinedTextField(
            value = email_val,
            onValueChange = { input ->
                email_val = input
            },
            label = { Text("Enter email address") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        OutlinedTextField(
            value = password_val,
            onValueChange = { input ->
                password_val = input
            },
            label = { Text("Enter password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (email_val.contains("@vitstudent.ac.in") || email_val.contains("@vit.ac.in")){
                    userType = 1
                }
                else if (email_val.contains("admin")){
                    userType = 2
                }
                else if(email_val.contains("driver")){
                    userType = 3
                }

                auth.signInWithEmailAndPassword(email_val, password_val)
                    .addOnCompleteListener { task ->
                        if (task.isSuccessful) {
                            Log.d("log in case passed", "waiting for navigation")
                            if (userType == 1) {
                                navController.navigate("student_home/$email_val")
                            }
                            else if (userType == 2){
                                navController.navigate("admin_home/$email_val")
                            }
                            else if (userType == 3){
                                navController.navigate("driver_home/$email_val")
                            }
                        }
                        else {
                            Log.d("log in case failed", "cannot navigate")
                            errorMessage = task.exception?.message

                        }
                    }
            },
            enabled = (email_val.isNotEmpty() and password_val.isNotEmpty())
        ) { Text(text = "Login") }

        Text(
            text = "Forgot password",
            color = Color.Blue,
            modifier = Modifier.clickable {
                navController.navigate("password_reset")
            }
        )
    }
}

fun saveFcmTokenToFirestore(email: String) {
    val db = FirebaseFirestore.getInstance()

    FirebaseMessaging.getInstance().token
        .addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val fcmToken = task.result

                db.collection("students").document(email)
                    .update("fcmToken", fcmToken)
                    .addOnSuccessListener {
                        Log.d("FCM", "FCM Token saved successfully")
                    }
                    .addOnFailureListener {
                        Log.e("FCM", "Failed to save token: ${it.message}")
                    }
            } else {
                Log.e("FCM", "Fetching FCM token failed", task.exception)
            }
        }
}
