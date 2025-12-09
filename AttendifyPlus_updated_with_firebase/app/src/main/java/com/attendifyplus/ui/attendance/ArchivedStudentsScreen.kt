package com.attendifyplus.ui.attendance

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Restore
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.StudentEntity
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@Composable
fun ArchivedStudentsScreen(
    navController: NavController,
    viewModel: ArchivedStudentsViewModel = getViewModel()
) {
    SetSystemBarIcons(useDarkIcons = true)
    val archivedStudents by viewModel.archivedStudents.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
            .statusBarsPadding()
            .padding(24.dp)
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 16.dp),
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
                text = "Archived Students",
                style = MaterialTheme.typography.h5.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
            )
        }

        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (archivedStudents.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(top = 48.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("No archived students found.", color = Color.Gray)
                    }
                }
            } else {
                items(archivedStudents) { student ->
                    ArchivedStudentCard(student = student, onRestore = { viewModel.restoreStudent(it) })
                }
            }
        }
    }
}

@Composable
fun ArchivedStudentCard(student: StudentEntity, onRestore: (String) -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        elevation = 1.dp,
        modifier = Modifier.fillMaxWidth(),
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = "${student.firstName} ${student.lastName}",
                    style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = MaterialTheme.colors.onSurface)
                )
                Text(
                    text = "ID: ${student.id}",
                    style = MaterialTheme.typography.caption.copy(color = MaterialTheme.colors.onSurface.copy(alpha = 0.6f))
                )
            }
            Button(
                onClick = { onRestore(student.id) },
                colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryBlue)
            ) {
                Icon(Icons.Default.Restore, contentDescription = "Restore", tint = Color.White)
                Spacer(Modifier.width(8.dp))
                Text("Restore", color = Color.White)
            }
        }
    }
}