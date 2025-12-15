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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SchoolCalendarConfigEntity
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel
import java.text.DateFormatSymbols
import java.util.*

/**
 * Read-only version of SchoolCalendarScreen for Teachers and Students.
 * Removes edit/add buttons and configuration logic.
 */
@Composable
fun ReadOnlySchoolCalendarScreen(
    navController: NavController,
    viewModel: SchoolCalendarViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = true)

    val config by viewModel.config.collectAsState()
    val events by viewModel.events.collectAsState()

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
        }

        Box(modifier = Modifier.weight(1f)) {
            if (config != null) {
                MonthListView(
                    config = config!!,
                    events = events,
                    navController = navController
                )
            } else {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text(
                        text = "Calendar not configured by Admin yet.",
                        style = MaterialTheme.typography.body1,
                        color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f)
                    )
                }
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
                        // Navigate to monthly view. 
                        // Note: MonthlyEventsScreen might also need a read-only mode if it allows editing.
                        // Assuming for now user only wants to block the main config screen.
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
