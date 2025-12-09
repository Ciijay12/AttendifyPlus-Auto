package com.attendifyplus

/**
 * Application TODO List & Feature Implementation Plan
 *
 * 1. Teacher can record attendance for previous days
 *    - [ ] Modify ManualAttendanceScreen to include a DatePicker.
 *    - [ ] Update AttendanceViewModel.recordManual to accept a specific timestamp (Long) instead of using System.currentTimeMillis().
 *    - [ ] Ensure validation logic respects the selected date (e.g., student enrollment at that time).
 *
 * 2. Teacher can print the QR Code of a student
 *    - [ ] Add a "Print QR" button in the Student Details or Student List screen.
 *    - [ ] Implement a feature to generate a high-quality Bitmap of the QR code.
 *    - [ ] Use Android's PrintManager or share the image via Intent (e.g., to a printer app or email).
 *    - [ ] Consider generating a PDF with the student's name and QR code for easier printing.
 *
 * 3. Instructions for importing CSV files of students
 *    - [ ] Create a new screen "ImportStudentsScreen" or a dialog instructions.
 *    - [ ] Define the required CSV format:
 *          Header: id,firstName,lastName,grade,section
 *          Example: S123,John,Doe,10,A
 *    - [ ] Add a "Select CSV File" button using ActivityResultContracts.GetContent().
 *    - [ ] Implement a CSV parser (can use a library or simple string splitting).
 *    - [ ] Iterate through the parsed data and insert into StudentRepository.
 *    - [ ] Provide feedback on success/failure (e.g., "Imported 20 students", "Error on line 5").
 */
object AppTodoList {
    // This object serves as a documentation holder for the requested features.
}
