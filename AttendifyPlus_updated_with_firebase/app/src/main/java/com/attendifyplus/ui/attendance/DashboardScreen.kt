package com.attendifyplus.ui.attendance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavController

@Composable
fun DashboardScreen(
    navController: NavController,
    role: String,
    onTeachers: () -> Unit,
    onManageClasses: () -> Unit,
    onAdvisoryDetails: () -> Unit,
    onClassDashboard: (String, String, String) -> Unit,
    userName: String = "" // Added optional userName parameter
) {
    when (role) {
        "student" -> {
            // The StudentDashboardScreen is now the top-level composable for the student role.
            // This DashboardScreen composable will no longer be called by StudentDashboardScreen.
        }
        "teacher", "adviser", "subject" -> TeacherDashboard(
            navController = navController,
            userName = userName // Pass it down
        )
        "admin" -> AdminDashboard(
            navController = navController,
            onTeachers = onTeachers,
            onConfig = onManageClasses
        )
        else -> {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("Unknown Role: $role")
            }
        }
    }
}