package com.attendifyplus.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Edit
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel
import java.text.DateFormatSymbols
import java.util.*

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun SchoolCalendarScreen(
    navController: NavController,
    viewModel: SchoolCalendarViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = true)

    val config by viewModel.config.collectAsState()
    val events by viewModel.events.collectAsState()
    var isEditing by remember { mutableStateOf(false) }

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
                text = "School Calendar",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
            )
            Spacer(Modifier.weight(1f))
            if (config != null && !isEditing) {
                IconButton(onClick = { isEditing = true }) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit School Year", tint = MaterialTheme.colors.onSurface)
                }
            }
        }

        Box(modifier = Modifier.weight(1f)) {
            if (config == null || isEditing) {
                SchoolYearConfigForm(
                    initialConfig = config,
                    onSave = { year, startMonth, endMonth ->
                        viewModel.saveConfig(year, startMonth, endMonth)
                        isEditing = false
                    },
                    onCancel = { if (config != null) isEditing = false else navController.popBackStack() }
                )
            } else {
                MonthListView(
                    config = config!!,
                    events = events,
                    navController = navController
                )
            }
        }
    }
}

@Composable
private fun MonthListView(
    config: SchoolCalendarConfigEntity,
    events: List<SchoolEventEntity>,
    navController: NavController
) {
    val months = remember(config) {
        val monthList = mutableListOf<Pair<Int, Int>>()
        val calendar = Calendar.getInstance().apply {
            clear()
            set(Calendar.YEAR, config.schoolYear.split("-").first().toInt())
            set(Calendar.MONTH, config.startMonth - 1)
        }
        val endCalendar = Calendar.getInstance().apply {
            clear()
            val startYear = config.schoolYear.split("-").first().toInt()
            val endYear = if (config.startMonth > config.endMonth) startYear + 1 else startYear
            set(Calendar.YEAR, endYear)
            set(Calendar.MONTH, config.endMonth - 1)
        }

        while (!calendar.after(endCalendar)) {
            monthList.add(Pair(calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH) + 1))
            calendar.add(Calendar.MONTH, 1)
        }
        monthList
    }

    val eventsByMonth = remember(events) {
        events.groupBy {
            val cal = Calendar.getInstance()
            cal.timeInMillis = it.date
            Pair(cal.get(Calendar.YEAR), cal.get(Calendar.MONTH) + 1)
        }
    }

    Column {
        Text(
            "School Year ${config.schoolYear}",
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface.copy(alpha = 0.7f)),
            modifier = Modifier.padding(bottom = 16.dp)
        )
        LazyVerticalGrid(
            columns = GridCells.Fixed(2),
            contentPadding = PaddingValues(bottom = 16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(months) { (year, month) ->
                val eventCount = eventsByMonth[Pair(year, month)]?.size ?: 0
                MonthCard(
                    year = year,
                    month = month,
                    eventCount = eventCount,
                    onClick = {
                        navController.navigate("monthly_events/$year/$month")
                    }
                )
            }
        }
    }
}

@Composable
private fun MonthCard(year: Int, month: Int, eventCount: Int, onClick: () -> Unit) {
    val monthName = remember { DateFormatSymbols().months[month - 1] }
    Card(
        shape = RoundedCornerShape(16.dp),
        modifier = Modifier
            .aspectRatio(1 / 0.7f)
            .clickable(onClick = onClick),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = monthName,
                style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
            )
            Text(
                text = year.toString(),
                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = if (eventCount == 1) "1 Event" else "$eventCount Events",
                style = MaterialTheme.typography.body2.copy(color = PrimaryBlue.copy(alpha = 0.8f))
            )
        }
    }
}

@OptIn(ExperimentalMaterialApi::class)
@Composable
private fun SchoolYearConfigForm(
    initialConfig: SchoolCalendarConfigEntity?,
    onSave: (String, Int, Int) -> Unit,
    onCancel: () -> Unit
) {
    var schoolYear by remember { mutableStateOf(initialConfig?.schoolYear ?: "") }
    var startMonth by remember { mutableStateOf(initialConfig?.startMonth) }
    var endMonth by remember { mutableStateOf(initialConfig?.endMonth) }
    
    val months = remember { (1..12).map { DateFormatSymbols().months[it - 1] } }
    var startMonthExpanded by remember { mutableStateOf(false) }
    var endMonthExpanded by remember { mutableStateOf(false) }

    val isFormValid = schoolYear.isNotBlank() && startMonth != null && endMonth != null

    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            if (initialConfig == null) "Setup School Year" else "Edit School Year",
            style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
        )
        Text(
            "Define the academic year and its duration.",
            style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)),
            modifier = Modifier.padding(bottom = 32.dp)
        )
        
        OutlinedTextField(
            value = schoolYear,
            onValueChange = { schoolYear = it },
            label = { Text("School Year (e.g., 2024-2025)") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
             colors = TextFieldDefaults.outlinedTextFieldColors(
                textColor = MaterialTheme.colors.onSurface
            )
        )
        Spacer(Modifier.height(16.dp))
        
        ExposedDropdownMenuBox(
            expanded = startMonthExpanded,
            onExpandedChange = { startMonthExpanded = !startMonthExpanded }
        ) {
            OutlinedTextField(
                value = startMonth?.let { months[it - 1] } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("Start Month") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = startMonthExpanded) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                 colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = startMonthExpanded,
                onDismissRequest = { startMonthExpanded = false }
            ) {
                months.forEachIndexed { index, monthName ->
                    DropdownMenuItem(onClick = {
                        startMonth = index + 1
                        startMonthExpanded = false
                    }) {
                        Text(monthName)
                    }
                }
            }
        }
        Spacer(Modifier.height(16.dp))
        
        ExposedDropdownMenuBox(
            expanded = endMonthExpanded,
            onExpandedChange = { endMonthExpanded = !endMonthExpanded }
        ) {
            OutlinedTextField(
                value = endMonth?.let { months[it - 1] } ?: "",
                onValueChange = {},
                readOnly = true,
                label = { Text("End Month") },
                trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = endMonthExpanded) },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp),
                 colors = TextFieldDefaults.outlinedTextFieldColors(
                    textColor = MaterialTheme.colors.onSurface
                )
            )
            ExposedDropdownMenu(
                expanded = endMonthExpanded,
                onDismissRequest = { endMonthExpanded = false }
            ) {
                months.forEachIndexed { index, monthName ->
                    DropdownMenuItem(onClick = {
                        endMonth = index + 1
                        endMonthExpanded = false
                    }) {
                        Text(monthName)
                    }
                }
            }
        }

        Spacer(Modifier.weight(1f))

        Button(
            onClick = { onSave(schoolYear, startMonth!!, endMonth!!) },
            modifier = Modifier.fillMaxWidth().height(50.dp),
            enabled = isFormValid,
            shape = RoundedCornerShape(12.dp),
            colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
        ) {
            Text("Save Configuration", color = Color.White)
        }
        
        TextButton(
            onClick = onCancel,
            modifier = Modifier.fillMaxWidth().padding(top = 8.dp)
        ) {
            Text("Cancel", color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
        }
    }
}
