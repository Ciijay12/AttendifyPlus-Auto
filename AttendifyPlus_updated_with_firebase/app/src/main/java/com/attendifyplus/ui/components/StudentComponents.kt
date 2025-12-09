package com.attendifyplus.ui.components

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendifyplus.ui.theme.Dimens
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SuccessGreen

@Composable
fun StatusOfTheDayCardForStudent(dailyStatus: String) {
    val statusColor = when {
        dailyStatus.contains("Class Day", ignoreCase = true) -> SuccessGreen
        dailyStatus.contains("Suspended", ignoreCase = true) -> Color.Red
        dailyStatus.contains("Cancelled", ignoreCase = true) -> Color.Red
        dailyStatus.contains("No Class", ignoreCase = true) -> Color.Red
        dailyStatus.contains("Holiday", ignoreCase = true) -> PrimaryBlue
        else -> Color.Gray
    }

    Card(
        shape = RoundedCornerShape(Dimens.CornerRadiusLarge),
        elevation = Dimens.CardElevation,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = Color.White
    ) {
        Column(
            modifier = Modifier.padding(Dimens.PaddingMedium)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Status of the Day",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue)
                )
                Icon(Icons.Default.Info, contentDescription = "Info", tint = PrimaryBlue)
            }
            
            Spacer(Modifier.height(Dimens.PaddingSmall))

            Text(
                text = dailyStatus,
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = statusColor),
                modifier = Modifier.padding(vertical = Dimens.PaddingSmall)
            )
        }
    }
}
