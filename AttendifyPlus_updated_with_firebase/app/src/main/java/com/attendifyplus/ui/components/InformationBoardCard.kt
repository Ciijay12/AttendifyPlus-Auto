package com.attendifyplus.ui.components

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.data.local.entities.SchoolPeriodEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal
import com.attendifyplus.ui.theme.SuccessGreen
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun InformationBoardCard(
    upcomingEvents: List<SchoolEventEntity>,
    currentPeriod: SchoolPeriodEntity?,
    dailyStatus: String,
    onViewCalendar: () -> Unit,
    onEditStatus: (() -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 4.dp,
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .animateContentSize() // Animate changes in content size
        ) {
            // 0. Title
            Text(
                text = "Information Board",
                style = MaterialTheme.typography.h6,
                color = MaterialTheme.colors.onSurface,
                modifier = Modifier.padding(start = 16.dp, top = 16.dp, bottom = 8.dp)
            )

            // 1. Upcoming Events Section
            EventsSection(events = upcomingEvents, onViewCalendar = onViewCalendar)

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f), thickness = 1.dp)

            // 2. Academic Period Section
            AcademicPeriodSection(period = currentPeriod)

            Divider(color = MaterialTheme.colors.onSurface.copy(alpha = 0.1f), thickness = 1.dp)

            // 3. Daily Status Section
            DailyStatusSection(status = dailyStatus, onEdit = onEditStatus)
        }
    }
}

@Composable
private fun EventsSection(events: List<SchoolEventEntity>, onViewCalendar: () -> Unit) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                "Upcoming Events",
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.onSurface
            )
            TextButton(onClick = onViewCalendar) {
                Text("View All")
            }
        }

        if (events.isEmpty()) {
            Text(
                "No upcoming events scheduled.",
                color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f),
                modifier = Modifier.padding(vertical = 16.dp)
            )
        } else {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.padding(top = 8.dp)
            ) {
                events.forEach { event ->
                    EventRow(event)
                }
            }
        }
    }
}

@Composable
private fun AcademicPeriodSection(period: SchoolPeriodEntity?) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            Icons.Default.DateRange,
            contentDescription = null,
            tint = SecondaryTeal,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Academic Period",
                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            )
            Text(
                text = period?.let { "S.Y. ${it.schoolYear}" } ?: "Not Set",
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold),
                color = MaterialTheme.colors.onSurface
            )
        }
        
        // Pill on the right
        if (period != null && period.currentPeriod.isNotBlank()) {
            Spacer(Modifier.width(8.dp))
            Surface(
                color = Color(0xFFFFE0B2), // Pastel Orange
                shape = RoundedCornerShape(12.dp),
            ) {
                Text(
                    text = period.currentPeriod,
                    style = MaterialTheme.typography.caption.copy(fontWeight = FontWeight.Bold),
                    color = Color(0xFFE65100),
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }
    }
}

@Composable
private fun DailyStatusSection(status: String, onEdit: (() -> Unit)?) {
    val isClassDay = status.equals("Class Day", ignoreCase = true) || status.equals("Classes Ongoing", ignoreCase = true)
    
    val (icon, color, text) = if (isClassDay) {
        Triple(Icons.Default.CheckCircle, SuccessGreen, "Classes Ongoing")
    } else {
        Triple(Icons.Default.Warning, Color.Red, "No Class - $status")
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(if (onEdit != null) Modifier.clickable(onClick = onEdit) else Modifier)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = color,
            modifier = Modifier.size(24.dp)
        )
        Spacer(Modifier.width(16.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                "Daily Class Status",
                style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
            )
            Text(
                text = text,
                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Bold, color = color)
            )
        }
        if (onEdit != null) {
            Icon(Icons.Default.Edit, contentDescription = "Edit", tint = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
        }
    }
}

@Composable
private fun EventRow(event: SchoolEventEntity) {
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
    val date = Date(event.date)

    Row(verticalAlignment = Alignment.CenterVertically) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .background(PrimaryBlue.copy(alpha = 0.1f))
                .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
            Text(dayFormat.format(date), fontWeight = FontWeight.Bold, color = PrimaryBlue, fontSize = 16.sp)
            Text(monthFormat.format(date).uppercase(), fontWeight = FontWeight.SemiBold, color = PrimaryBlue, fontSize = 10.sp)
        }
        Spacer(Modifier.width(16.dp))
        Text(
            event.title,
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onSurface
        )
    }
}
