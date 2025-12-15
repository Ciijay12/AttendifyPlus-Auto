package com.attendifyplus.ui.attendance

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.attendifyplus.ui.components.GlobalUpdateHandler
import com.attendifyplus.ui.settings.DebugSettingsScreen
import org.koin.androidx.compose.getViewModel
import java.net.URLDecoder
import java.net.URLEncoder

@Composable
fun NavHostContainer(viewModel: LoginViewModel = getViewModel()) {
    val nav = rememberNavController()
    val isLoggedIn by viewModel.isLoggedIn.collectAsState()
    val userRole by viewModel.userRole.collectAsState()
    val userId by viewModel.userId.collectAsState()

    // Add Global Update Handler
    GlobalUpdateHandler()

    NavHost(
        navController = nav,
        startDestination = "launcher",
        enterTransition = { 
            slideInHorizontally(initialOffsetX = { 1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) 
        },
        exitTransition = { 
            slideOutHorizontally(targetOffsetX = { -1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) 
        },
        popEnterTransition = { 
            slideInHorizontally(initialOffsetX = { -1000 }, animationSpec = tween(500)) + fadeIn(animationSpec = tween(500)) 
        },
        popExitTransition = { 
            slideOutHorizontally(targetOffsetX = { 1000 }, animationSpec = tween(500)) + fadeOut(animationSpec = tween(500)) 
        }
    ) {
        composable("launcher") {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            
            LaunchedEffect(isLoggedIn, userRole, userId) {
                val destination = if (isLoggedIn && userRole != null) {
                    when (userRole) {
                        "teacher", "adviser", "subject" -> "dashboard/teacher"
                        "student" -> "dashboard/student/$userId"
                        "admin" -> "dashboard/admin"
                        else -> "login"
                    }
                } else {
                    "login"
                }

                nav.navigate(destination) {
                    popUpTo("launcher") { inclusive = true }
                }
            }
        }

        composable("login") { 
            LoginScreen(onLogin = { role, id -> 
                viewModel.checkLoginStatus() // Re-check to update state
                
                val destination = when (role) {
                    "teacher", "adviser", "subject" -> "dashboard/teacher"
                    "student" -> "dashboard/student/$id"
                    "admin" -> "dashboard/admin"
                    else -> "dashboard/$role" // Fallback
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
            val onLogoutAction = {
                viewModel.onLogout()
                nav.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }

            when (role) {
                "teacher", "adviser", "subject" -> TeacherDashboardScreen(
                    navController = nav,
                    onLogout = onLogoutAction
                )
                "admin" -> AdminDashboardScreen(
                    navController = nav,
                    role = role,
                    onLogout = onLogoutAction
                )
                else -> {
                     // If role is unknown, force logout
                    LaunchedEffect(Unit) {
                        onLogoutAction()
                    }
                }
            }
        }

        // New route for student dashboard with studentId
        composable(
            "dashboard/student/{studentId}",
            arguments = listOf(
                navArgument("studentId") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val studentId = backStackEntry.arguments?.getString("studentId") ?: ""
            val onLogoutAction = {
                viewModel.onLogout()
                nav.navigate("login") {
                    popUpTo(0) { inclusive = true }
                }
            }

            StudentDashboardScreen(
                navController = nav,
                role = "student",
                studentId = studentId,
                onLogout = onLogoutAction
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
        composable("student_export") { StudentExportScreen(navController = nav) }
        composable("manage_classes") { ManageClassesScreen(navController = nav) }
        composable("history") { StudentHistoryScreen(navController = nav) }
        composable("teachers") { TeacherListScreen(onBack = { nav.popBackStack() }, onTeacherClick = { nav.navigate("teacher_detail/$it") }) }
        composable("teacher_detail/{teacherId}") { backStackEntry ->
            TeacherDetailScreen(navController = nav, teacherId = backStackEntry.arguments?.getString("teacherId") ?: "")
        }
        composable("school_calendar") { SchoolCalendarScreen(navController = nav) }
        composable("academic_periods") { AcademicPeriodsScreen(navController = nav) }
        composable("admin_student_management") { AdminStudentManagementScreen(navController = nav) }
        composable("admin_grade_management") { AdminGradeManagementScreen(navController = nav) }
        
        // New Route for Grade Detail
        composable(
            "admin_grade_detail/{grade}",
            arguments = listOf(navArgument("grade") { type = NavType.StringType })
        ) { backStackEntry ->
            val grade = backStackEntry.arguments?.getString("grade") ?: ""
            AdminGradeDetailScreen(navController = nav, grade = grade)
        }

        composable(
            "admin_advisory_detail/{grade}/{section}",
            arguments = listOf(
                navArgument("grade") { type = NavType.StringType },
                navArgument("section") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val grade = backStackEntry.arguments?.getString("grade") ?: ""
            val section = backStackEntry.arguments?.getString("section") ?: ""
            AdminAdvisoryDetailScreen(navController = nav, grade = grade, section = section)
        }

        composable("admin_subject_management") {
            AdminSubjectManagementScreen(navController = nav)
        }
        
        composable("config_import_export") {
            ConfigScreen(navController = nav)
        }
        
        // Track Configuration Route
        composable("track_configuration") {
            TrackConfigurationScreen(navController = nav)
        }

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
        composable("archived_students") { ArchivedStudentsScreen(navController = nav) }

        // Debug Settings
        composable("debug_settings") { DebugSettingsScreen(navController = nav) }
    }
}
