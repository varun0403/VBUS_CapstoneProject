package com.example.vbus.authenication

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.google.firebase.Firebase
import com.google.firebase.auth.*

@Composable
fun SignUpScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var signUpError by remember { mutableStateOf<String?>(null) }
    val auth = Firebase.auth

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text("Sign Up Screen")

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Enter email address") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Enter password") }
        )

        Button(onClick = {
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        navController.navigate("login")
                    }
                    else {
                        // Sign up failed, show error message
                        signUpError = task.exception?.message ?: "Sign-up failed"
                    }
                }
        }) {
            Text(text = "Sign Up")
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Display error message if sign-up fails
        signUpError?.let { errorMessage ->
            Text(text = errorMessage, color = Color.Red, modifier = Modifier.padding(8.dp))
        }
    }
}
