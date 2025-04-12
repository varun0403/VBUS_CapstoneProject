package com.example.vbus

import android.annotation.SuppressLint
import android.os.Build
import android.os.Bundle
import android.os.StrictMode
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.annotation.RequiresApi
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Switch
import androidx.compose.material.SwitchDefaults
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBox
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.vbus.admin.*
import com.example.vbus.authenication.AuthScreen
import com.example.vbus.authenication.LoginScreen
import com.example.vbus.authenication.PasswordChangeScreen
import com.example.vbus.authenication.SignUpScreen
import com.example.vbus.driver.DriverHomeScreen
import com.example.vbus.driver.DriverMapScreen
import com.example.vbus.driver.DriverRescueUi
import com.example.vbus.student.*
import com.example.vbus.ui.theme.VBUSTheme
import com.example.vbus.student.CustomCard
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.GeoPoint
import com.google.firebase.firestore.ktx.firestore
import kotlinx.coroutines.delay
import com.google.firebase.ktx.Firebase

class MainActivity : ComponentActivity() {
    @SuppressLint("MissingSuperCall")
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContent {
            VBUSTheme{
                val policy = StrictMode.ThreadPolicy.Builder().permitAll().build()
                StrictMode.setThreadPolicy(policy)
                val navController = rememberNavController()
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    NavHost(
                        navController = navController,
                        startDestination = "base",
                        modifier = Modifier.padding(innerPadding)
                    ) {
                        composable("base") { Base(navController) }
                        composable("auth") { AuthScreen(navController) }
                        composable("password_reset"){ PasswordChangeScreen(navController) }
                        composable(
                            "student_home/{email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email").orEmpty()
                            StudentHomeScreen(navController, email)
                        }

                        composable(
                            "complaint_screen/{name}",
                            arguments = listOf(navArgument("name") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            StudentComplaintScreen(navController, name)
                        }

                        composable(
                            "complaint_posting/{name}",
                            arguments = listOf(navArgument("name") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            PostComplaint(navController, name)
                        }

                        composable(
                            "complaint_tracking/{name}",
                            arguments = listOf(navArgument("name") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            TrackComplaints(navController, name)
                        }

                        composable(
                            "admin_home/{name}",
                            arguments = listOf(navArgument("name") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val name = backStackEntry.arguments?.getString("name").orEmpty()
                            AdminHomeScreen(navController, name)
                        }
                        composable("admin_complaint_home") {
                            AdminComplaintScreen(navController)
                        }
                        composable("admin_complaint_pending") {
                            AdminComplaintPending(navController)
                        }
                        composable("admin_complaint_resolved") {
                            AdminComplaintResolved(navController)
                        }
                        composable(
                            route = "letter_generation/{email}",
                            arguments = listOf(navArgument("email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("email").orEmpty()
                            GenerateLateArrivalLetter(navController,email,context = applicationContext)
                        }
                        composable("admin_announcement") {
                            AdminAnnouncementScreen()
                        }
                        composable("student_announcement") {
                            StudentAnnouncementScreen(navController)
                        }
                        composable("track_bus/{bus_no}/{email}",
                            arguments = listOf(navArgument("bus_no") { type = NavType.StringType })
                        ) {
                            backStackEntry ->
                            val bus_no = backStackEntry.arguments?.getString("bus_no").orEmpty()
                            val email = backStackEntry.arguments?.getString("email").orEmpty()
                            MyMapScreen(bus_no,email)
                        }
                        composable("driver_rescue_map/{bus_no}",
                            arguments = listOf(navArgument("bus_no") { type = NavType.StringType })
                        ) {
                            backStackEntry ->
                            val bus_no = backStackEntry.arguments?.getString("bus_no").orEmpty()
                            DriverRescueUi(bus_no)
                        }
                        composable(
                            "driver_home/{driver_email}",
                            arguments = listOf(navArgument("driver_email") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val email = backStackEntry.arguments?.getString("driver_email").orEmpty()
                            DriverHomeScreen(navController, email)
                        }

                        composable("driver_loc/{bus_no}",
                            arguments = listOf(navArgument("bus_no") { type = NavType.StringType })
                        ) {
                            backStackEntry ->
                            val bus_no = backStackEntry.arguments?.getString("bus_no").orEmpty()
                            DriverMapScreen(navController,bus_no)
                        }
                        composable("fetch_student_data") {
                            FetchStudentDataScreen()
                        }
                        composable("camera_screen"){
                            CameraScreen()
                        }
                        composable("admin_bus_viewer"){
                            AdminBusStatusScreen()
                        }
                        composable("map_test_screen/{bus_no}",
                            arguments = listOf(navArgument("bus_no") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val bus_no = backStackEntry.arguments?.getString("bus_no").orEmpty()
                            MapTestScreen(bus_no)
                        }
                        composable("admin_tracking"){
                            AdminMapScreen()
                        }
                    }
                }
            }
        }
    }
}

@SuppressLint("MutableCollectionMutableState")
@Composable
fun Base(navController: NavController) {
    LaunchedEffect(Unit) {
        delay(3000) // Show splash for 3 seconds
        navController.navigate("auth") { popUpTo(0) } // Navigate to auth and clear stack
    }

    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "VBUS",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

