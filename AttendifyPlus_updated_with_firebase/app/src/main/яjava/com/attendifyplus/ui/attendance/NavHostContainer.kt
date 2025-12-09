package com.attendifyplus.ui.attendance

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavHostContainer() {
    val nav = rememberNavController()
    
    NavHost(
        navController = nav, 
        startDestination = "login"
    ) {
        composable("login") { 
            LoginScreen(onLogin = { role -> 
                val destination = when (role) {
                    "teacher" -> "dashboard/teacher"
                    "student" -> "dashboard/student"
                    "admin" -> "dashboard/admin"
                    else -> "login"
                }
                nav.navigate(destination) {
                    popUpTo("login") { inclusive = true }
                }
            }) 
        }
        
        composable(
            "dashboard/{role}",
            arguments = listOf(navArgument("role") { type = NavType.StringType })
        ) { backStackEntry ->
            val role = backStackEntry.arguments?.getString("role") ?: ""
            DashboardScreen(
                navController = nav,
                role = role,
                onTeachers = { nav.navigate("teachers") },
                onManageClasses = { nav.navigate("manage_classes") },
                onAdvisoryDetails = { /* Not implemented at this level */ },
                onClassDashboard = { subject, grade, section ->
                    val encSubject = URLEncoder.encode(subject, "UTF-8")
                    val encGrade = URLEncoder.encode(grade, "UTF-8")
                    val encSection = URLEncoder.encode(section, "UTF-8")
                    nav.navigate("class_dashboard/$encSubject/$encGrade/$encSection")
                }
            )
        }

        composable(
            "class_dashboard/{subject}/{grade}/{section}",
            arguments = listOf(
                navArgument("subject") { type = NavType.StringType },
                navArgument("grade") { type = NavType.StringType },
                navArgument("section") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val subject = URLDecoder.decode(backStackEntry.arguments?.getString("subject") ?: "", "UTF-8")
            val grade = URLDecoder.decode(backStackEntry.arguments?.getString("grade") ?: "", "UTF-8")
            val section = URLDecoder.decode(backStackEntry.arguments?.getString("section") ?: "", "UTF-8")
            
            ClassDashboardScreen(
                navController = nav,
                subjectName = subject,
                grade = grade,
                section = section
            )
        }

        composable("scan") { QRAttendanceScreen(navController = nav) }
        composable("manual") { ManualAttendanceScreen(navController = nav) }
        composable("retroactive_attendance") { RetroactiveAttendanceScreen(navController = nav) }
        composable("export_attendance") { AttendanceExportScreen(navController = nav) }
        composable("manage_classes") { ManageClassesScreen(navController = nav, role = "teacher") }
        composable("history") { StudentHistoryScreen(navController = nav) }
        composable("teachers") { TeacherListScreen(onBack = { nav.popBackStack() }, onTeacherClick = { nav.navigate("teacher_detail/$it") }) }
        composable("teacher_detail/{teacherId}") { backStackEntry ->
            TeacherDetailScreen(navController = nav, teacherId = backStackEntry.arguments?.getString("teacherId") ?: "")
        }
        composable("school_calendar") { SchoolCalendarScreen(navController = nav) }
        composable("academic_periods") { AcademicPeriodsScreen(navController = nav) }
        composable(
            "monthly_events/{year}/{month}",
            arguments = listOf(
                navArgument("year") { type = NavType.IntType },
                navArgument("month") { type = NavType.IntType }
            )
        ) { backStackEntry ->
            val year = backStackEntry.arguments?.getInt("year") ?: 0
            val month = backStackEntry.arguments?.getInt("month") ?: 0
            MonthlyEventsScreen(navController = nav, year = year, month = month)
        }
    }
}