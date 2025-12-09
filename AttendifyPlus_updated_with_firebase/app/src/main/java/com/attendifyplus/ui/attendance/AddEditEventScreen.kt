package com.attendifyplus.ui.attendance

import android.app.DatePickerDialog
import android.widget.DatePicker
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendifyplus.ui.theme.PillShape
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AddEditEventContent(
    viewModel: SchoolCalendarViewModel = getViewModel(),
    onEventAdded: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var selectedDate by remember { mutableStateOf<Date?>(null) }
    val context = LocalContext.current
    val sdf = SimpleDateFormat("MMMM dd, yyyy", Locale.getDefault())

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _: DatePicker, year: Int, month: Int, dayOfMonth: Int ->
            calendar.set(year, month, dayOfMonth)
            selectedDate = calendar.time
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(24.dp)
    ) {
        Text(
            "Add New Event",
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )
        Text(
            "Enter the event information below.",
            style = MaterialTheme.typography.caption.copy(color = Color.Gray),
            modifier = Modifier.padding(bottom = 24.dp).align(Alignment.CenterHorizontally)
        )

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Event Title") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = description,
            onValueChange = { description = it },
            label = { Text("Description (Optional)") },
            modifier = Modifier
                .fillMaxWidth()
                .height(120.dp),
            shape = RoundedCornerShape(12.dp)
        )
        Spacer(Modifier.height(16.dp))

        OutlinedButton(
            onClick = { datePickerDialog.show() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(12.dp),
            border = BorderStroke(1.dp, Color.Gray.copy(alpha = 0.5f))
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.CalendarToday,
                    contentDescription = "Select Date",
                    tint = PrimaryBlue
                )
                Spacer(Modifier.width(12.dp))
                Text(
                    text = selectedDate?.let { sdf.format(it) } ?: "Select a date",
                    color = if (selectedDate != null) Color.Black else Color.Gray,
                    fontSize = 16.sp
                )
            }
        }
        Spacer(Modifier.height(24.dp))

        Button(
            onClick = {
                if (title.isNotBlank() && selectedDate != null) {
                    viewModel.addEvent(title, description, selectedDate!!)
                    onEventAdded()
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp),
            shape = PillShape,
            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue),
            enabled = title.isNotBlank() && selectedDate != null
        ) {
            Text("Save Event", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
        }
    }
}
