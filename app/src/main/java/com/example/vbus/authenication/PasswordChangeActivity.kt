package com.example.vbus.authenication

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.*
import androidx.compose.ui.platform.LocalContext
import com.example.vbus.notifications.MyFirebaseMessagingService

@Composable
fun PasswordChangeScreen(navController: NavController) {
    var email_val by remember { mutableStateOf("") }
    val auth: FirebaseAuth = Firebase.auth
    val context = LocalContext.current

    Column(modifier = Modifier.fillMaxSize().padding(16.dp),verticalArrangement = Arrangement.Center){
        Text(text = "Password change")

        OutlinedTextField(
            value = email_val,
            onValueChange = { input ->
                email_val = input
            },
            label = { Text("Enter email address") },
            modifier = Modifier.padding(bottom = 16.dp)
        )

        Button(
            onClick = {
                if (email_val.isNotEmpty()) {
                    auth.sendPasswordResetEmail(email_val)
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                email_val = " "
                            }
                        }
                }
            },
            enabled = (email_val.isNotEmpty() and email_val.contains("@vitstudent.ac.in"))
        ) { Text(text = "Change password") }

    }
}
