package com.attendifyplus.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun EventCard(event: SchoolEventEntity, onDelete: (SchoolEventEntity) -> Unit) {
    val date = Date(event.date)
    val dayFormat = SimpleDateFormat("dd", Locale.getDefault())
    val dayOfWeekFormat = SimpleDateFormat("EEE", Locale.getDefault())

    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(12.dp),
        elevation = 2.dp,
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Date Box
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .background(PrimaryBlue.copy(alpha = 0.1f))
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            ) {
                Text(
                    text = dayFormat.format(date),
                    fontWeight = FontWeight.Bold,
                    fontSize = 22.sp,
                    color = PrimaryBlue
                )
                Text(
                    text = dayOfWeekFormat.format(date).uppercase(),
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 12.sp,
                    color = PrimaryBlue.copy(alpha = 0.8f)
                )
            }

            Spacer(Modifier.width(16.dp))

            // Event Details
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = event.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 17.sp
                )
                if (!event.description.isNullOrBlank()) {
                    Text(
                        text = event.description,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            // Delete Button
            IconButton(onClick = { onDelete(event) }) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Delete Event",
                    tint = Color.Red.copy(alpha = 0.7f)
                )
            }
        }
    }
}
