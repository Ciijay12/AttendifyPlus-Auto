package com.attendifyplus.ui.attendance

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun AcademicPeriodsScreen(
    navController: NavController,
    viewModel: AcademicPeriodsViewModel = getViewModel()
) {
    // Ensure status bar icons are dark (black) for this light screen
    SetSystemBarIcons(useDarkIcons = true)

    val context = LocalContext.current
    val schoolPeriod by viewModel.schoolPeriod.collectAsState()
    val calendarConfig by viewModel.calendarConfig.collectAsState()
    val isModified by viewModel.isModified.collectAsState()
    
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("Junior High", "Senior High")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
         // Header
        Row(
            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = { navController.popBackStack() },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colors.onSurface)
            }
            Spacer(Modifier.width(16.dp))
            Text(
                text = "Academic Periods",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
            )
        }

        if (calendarConfig == null) {
             Box(
                modifier = Modifier
                    .fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Please configure School Calendar first.",
                    style = MaterialTheme.typography.h6,
                    color = Color.Gray
                )
            }
        } else {
            val schoolYear = calendarConfig!!.schoolYear
            Column(modifier = Modifier.fillMaxSize()) {
                TabRow(
                    selectedTabIndex = selectedTab,
                    backgroundColor = MaterialTheme.colors.background,
                    contentColor = PrimaryBlue
                ) {
                    tabs.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                        )
                    }
                }

                Column(modifier = Modifier.verticalScroll(rememberScrollState()).padding(top = 16.dp)) {
                    Text(
                        text = "School Year: $schoolYear",
                        style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                        modifier = Modifier.padding(bottom = 24.dp)
                    )

                    if (selectedTab == 0) { // JHS
                        (0..3).forEach { i ->
                            PeriodCard(
                                title = "Quarter ${i + 1}",
                                startDate = schoolPeriod?.let { it.q1Start.takeIf { i==0 } ?: it.q2Start.takeIf { i==1 } ?: it.q3Start.takeIf { i==2 } ?: it.q4Start },
                                endDate = schoolPeriod?.let { it.q1End.takeIf { i==0 } ?: it.q2End.takeIf { i==1 } ?: it.q3End.takeIf { i==2 } ?: it.q4End },
                                onStartDateSelected = { date -> viewModel.onDateChanged("jhsQ${i+1}Start", date) },
                                onEndDateSelected = { date -> viewModel.onDateChanged("jhsQ${i+1}End", date) }
                            )
                        }
                    } else { // SHS
                        OutlinedButton(
                            onClick = {
                                viewModel.applyJhsToShs()
                                Toast.makeText(context, "Applied JHS dates to Senior High", Toast.LENGTH_SHORT).show()
                            },
                            modifier = Modifier.fillMaxWidth().padding(bottom = 16.dp),
                            shape = RoundedCornerShape(12.dp),
                            colors = ButtonDefaults.outlinedButtonColors(
                                contentColor = MaterialTheme.colors.onSurface
                            )
                        ) {
                            Icon(Icons.Default.ContentCopy, contentDescription = null, modifier = Modifier.size(18.dp), tint = MaterialTheme.colors.onSurface)
                            Spacer(Modifier.width(8.dp))
                            Text("Apply JHS Schedule to SHS")
                        }

                        SemesterSection(title = "First Semester") {
                            (0..1).forEach { i ->
                                PeriodCard(
                                    title = "Quarter ${i + 1}",
                                    startDate = schoolPeriod?.let { it.shsQ1Start.takeIf { i==0 } ?: it.shsQ2Start },
                                    endDate = schoolPeriod?.let { it.shsQ1End.takeIf { i==0 } ?: it.shsQ2End },
                                    onStartDateSelected = { date -> viewModel.onDateChanged("shsQ${i+1}Start", date) },
                                    onEndDateSelected = { date -> viewModel.onDateChanged("shsQ${i+1}End", date) }
                                )
                            }
                        }
                        Spacer(Modifier.height(16.dp))
                        SemesterSection(title = "Second Semester") {
                            (2..3).forEach { i ->
                                PeriodCard(
                                    title = "Quarter ${i + 1}",
                                    startDate = schoolPeriod?.let { it.shsQ3Start.takeIf { i==2 } ?: it.shsQ4Start },
                                    endDate = schoolPeriod?.let { it.shsQ3End.takeIf { i==2 } ?: it.shsQ4End },
                                    onStartDateSelected = { date -> viewModel.onDateChanged("shsQ${i+1}Start", date) },
                                    onEndDateSelected = { date -> viewModel.onDateChanged("shsQ${i+1}End", date) }
                                )
                            }
                        }
                    }
                    
                    Spacer(Modifier.height(24.dp))

                    Button(
                        onClick = {
                            viewModel.savePeriods()
                            Toast.makeText(context, "Academic periods saved successfully", Toast.LENGTH_SHORT).show()
                            navController.popBackStack()
                        },
                        enabled = isModified,
                        modifier = Modifier.fillMaxWidth().height(50.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryBlue,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Save Academic Periods")
                    }
                    
                    Spacer(Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
private fun SemesterSection(title: String, content: @Composable ColumnScope.() -> Unit) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground),
            modifier = Modifier.padding(bottom = 8.dp)
        )
        content()
    }
}

@Composable
private fun PeriodCard(
    title: String,
    startDate: Long?,
    endDate: Long?,
    onStartDateSelected: (Long) -> Unit,
    onEndDateSelected: (Long) -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(title, style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface))
            Spacer(Modifier.height(16.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                DateSelector(modifier = Modifier.weight(1f), label = "Start Date", timestamp = startDate, onDateSelected = onStartDateSelected)
                DateSelector(modifier = Modifier.weight(1f), label = "End Date", timestamp = endDate, onDateSelected = onEndDateSelected)
            }
        }
    }
}

@Composable
private fun DateSelector(
    label: String,
    timestamp: Long?,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val sdf = remember { SimpleDateFormat("MMM dd, yyyy", Locale.getDefault()) }
    val formattedDate = remember(timestamp) {
        if (timestamp != null && timestamp > 0) sdf.format(Date(timestamp)) else "Set Date"
    }

    val calendar = Calendar.getInstance()
    if (timestamp != null && timestamp > 0) {
        calendar.timeInMillis = timestamp
    }
    
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            val cal = Calendar.getInstance().apply { set(year, month, day) }
            onDateSelected(cal.timeInMillis)
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Surface(
        modifier = modifier.clickable { datePickerDialog.show() },
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colors.onSurface.copy(alpha = 0.05f),
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                Icons.Default.DateRange, 
                contentDescription = null, 
                tint = PrimaryBlue,
                modifier = Modifier.size(20.dp)
            )
            Spacer(Modifier.width(8.dp))
            Column {
                Text(label, style = MaterialTheme.typography.caption.copy(fontSize = 10.sp, color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)))
                Text(formattedDate, fontWeight = FontWeight.SemiBold, fontSize = 14.sp, color = MaterialTheme.colors.onSurface)
            }
        }
    }
}
