# AttendifyPlus — Complete Attendance System

This project is a fully functional Android application for school attendance tracking, featuring:
- **Jetpack Compose UI** with role-based screens (Admin, Teacher/Adviser, Subject Teacher, Student).
- **Room Local Database** for offline-first capability.
- **Firebase Realtime Database** for two-way background synchronization.
- **QR Scanning & Generation** (CameraX + ML Kit) for fast attendance logging.
- **CSV Bulk Import** for easy student management.

## Status: Active & Integrated

### Features Implemented
- [x] **Role-Based Dashboards**: Tailored UIs for Advisers (Homeroom), Subject Teachers, and Students.
- [x] **Offline-First Architecture**: All data is stored locally in Room and synced when online.
- [x] **Background Sync**: `SyncWorker` is enabled and runs every 15 minutes (via WorkManager) to sync data with Firebase.
- [x] **QR Code System**: 
  - Generate QR codes for students from the directory.
  - Scan QR codes to mark attendance (Present/Late/Absent).
- [x] **CSV Import**: Import student lists directly from device storage.
- [x] **Student History**: Students can view their own attendance history.

### Configuration
The project is connected to Firebase project `aplus-attendance-app-822d8`.
- `google-services.json` is present in the `app/` module.
- Dependencies for Firebase Auth and Realtime Database are configured.

### Technical Details
- **Sync Strategy**: Last-write-wins based on `updatedAt` timestamp.
- **Database**: Firebase Realtime Database (JSON tree structure) for low-latency updates.
- **Dependency Injection**: Koin is used for injecting Repositories into ViewModels and Workers.
- **QR Payload**: Compact JSON format `{"t":"student","i":"ID"}`.

### Usage
1. **Login**: Select Teacher, Student, or Admin.
2. **Adviser Mode**: Create a homeroom class, manage students, and view daily summaries.
3. **Subject Mode**: Create multiple subject classes and track attendance per period.
4. **Scan**: Use the camera to scan student QR codes.
5. **Sync**: Occurs automatically in the background when connected to the internet.
