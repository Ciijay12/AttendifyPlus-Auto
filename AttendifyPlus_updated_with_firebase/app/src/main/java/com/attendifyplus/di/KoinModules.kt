package com.attendifyplus.di

import android.app.Application
import androidx.room.Room
import com.attendifyplus.data.local.AttendifyDatabase
import com.attendifyplus.data.repositories.AttendanceRepository
import com.attendifyplus.data.repositories.SchoolCalendarConfigRepository
import com.attendifyplus.data.repositories.SchoolEventRepository
import com.attendifyplus.data.repositories.SchoolPeriodRepository
import com.attendifyplus.data.repositories.StudentRepository
import com.attendifyplus.data.repositories.SubjectClassRepository
import com.attendifyplus.data.repositories.TeacherRepository
import com.attendifyplus.data.repositories.AdminSubjectRepository
import com.attendifyplus.data.repositories.SyncRepository
import com.attendifyplus.ui.attendance.*
import com.attendifyplus.ui.settings.DebugSettingsViewModel
import com.attendifyplus.util.NotificationHelper
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single {
        Room.databaseBuilder(get<Application>(), AttendifyDatabase::class.java, "attendify.db")
            .fallbackToDestructiveMigration() // This allows DB to be cleared if schema changes
            .build()
    }

    single { get<AttendifyDatabase>().studentDao() }
    single { get<AttendifyDatabase>().attendanceDao() }
    single { get<AttendifyDatabase>().teacherDao() }
    single { get<AttendifyDatabase>().subjectClassDao() }
    single { get<AttendifyDatabase>().schoolEventDao() }
    single { get<AttendifyDatabase>().schoolPeriodDao() }
    single { get<AttendifyDatabase>().schoolCalendarConfigDao() }
    single { get<AttendifyDatabase>().adminSubjectDao() }

    single { StudentRepository(get()) }
    single { AttendanceRepository(get(), get()) }
    single { TeacherRepository(get()) }
    single { SubjectClassRepository(get()) }
    single { SchoolEventRepository(get()) }
    single { SchoolPeriodRepository(get()) }
    single { SchoolCalendarConfigRepository(get()) }
    single { AdminSubjectRepository(get()) }
    single { SyncRepository(get(), androidContext()) }
    
    // Notification Helper
    single { NotificationHelper(androidContext()) }

    // Updated to inject context
    viewModel { AttendanceViewModel(get(), get(), get(), get(), get(), androidContext()) }
    viewModel { StudentListViewModel(get()) }
    viewModel { DashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { StudentHistoryViewModel(get()) }
    // Updated to inject context for Session Management
    viewModel { LoginViewModel(get(), get(), androidContext()) }
    viewModel { SubjectClassViewModel(get(), get(), androidContext()) } // Updated to inject TeacherRepository and Context
    viewModel { AdvisoryDetailsViewModel(get()) }
    viewModel { ClassDashboardViewModel(get(), get()) }
    viewModel { TeacherListViewModel(get(), get()) } // Injecting StudentRepository as well
    viewModel { TeacherDetailViewModel(get(), get()) }
    // Updated SchoolCalendarViewModel to inject SchoolPeriodRepository
    viewModel { SchoolEventsViewModel(get(), get(), get()) }
    viewModel { SchoolCalendarViewModel(get(), get(), get()) }
    viewModel { AcademicPeriodsViewModel(get(), get()) }
    viewModel { (year: Int, month: Int) -> MonthlyEventsViewModel(get(), year, month) }
    
    // New Role-Based ViewModels
    viewModel { StudentDashboardViewModel(get(), get(), get(), get(), get()) }
    viewModel { TeacherDashboardViewModel(get(), get(), get(), get(), get(), get()) }
    viewModel { AdminDashboardViewModel(get(), get(), get(), get(), get(), androidContext()) }
    
    // Feature 1: Retroactive Attendance ViewModel
    viewModel { RetroactiveAttendanceViewModel(get(), get()) }

    // New Admin Student Management
    viewModel { AdminStudentManagementViewModel(get(), get(), get(), androidContext()) }
    
    // Archived Students
    viewModel { ArchivedStudentsViewModel(get()) }

    // Admin Subject Management (Injected Context)
    viewModel { AdminSubjectManagementViewModel(get(), androidContext()) }
    
    // Debug Settings
    viewModel { DebugSettingsViewModel(get()) }
}
