package com.attendifyplus.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Class
import androidx.compose.material.icons.filled.Groups
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.attendifyplus.ui.theme.PrimaryBlue
import com.attendifyplus.ui.theme.SecondaryTeal

@Composable
fun RoleSelectionScreen(
    onAdvisorySelected: () -> Unit,
    onSubjectSelected: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxSize(),
        color = Color(0xFFF5F7FA) // Softer background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select Mode",
                style = MaterialTheme.typography.h4.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onBackground)
            )
            Spacer(Modifier.height(8.dp))
            Text(
                text = "How would you like to take attendance?",
                style = MaterialTheme.typography.subtitle1.copy(color = Color.Gray)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Advisory Card
            RoleCard(
                title = "Advisory Class",
                description = "Manage homeroom attendance",
                icon = Icons.Default.Groups,
                color = PrimaryBlue,
                onClick = onAdvisorySelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Subject Card
            RoleCard(
                title = "Subject Class",
                description = "Manage subject-specific classes",
                icon = Icons.Default.Class,
                color = SecondaryTeal,
                onClick = onSubjectSelected
            )
        }
    }
}

@Composable
fun RoleCard(
    title: String,
    description: String,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        elevation = 2.dp,
        modifier = Modifier
            .fillMaxWidth()
            .height(100.dp)
            .clickable(onClick = onClick),
        backgroundColor = Color.White
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                shape = androidx.compose.foundation.shape.CircleShape,
                color = color.copy(alpha = 0.1f),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }
            
            Spacer(Modifier.width(20.dp))
            
            Column(
                verticalArrangement = Arrangement.Center
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.h6.copy(fontWeight = FontWeight.Bold)
                )
                Text(
                    text = description,
                    style = MaterialTheme.typography.body2.copy(color = Color.Gray)
                )
            }
        }
    }
}
