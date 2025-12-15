package com.attendifyplus.ui.attendance

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.ui.theme.PrimaryBlue
import org.koin.androidx.compose.getViewModel

@Composable
fun TrackConfigurationScreen(
    navController: NavController,
    viewModel: AdminStudentManagementViewModel = getViewModel()
) {
    val enabledTracks by viewModel.enabledTracks.collectAsState()
    
    // Set status bar icons to dark since background is white
    SetSystemBarIcons(useDarkIcons = true)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Track Configuration", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                },
                backgroundColor = MaterialTheme.colors.surface,
                contentColor = MaterialTheme.colors.onSurface,
                elevation = 4.dp
            )
        },
        backgroundColor = MaterialTheme.colors.background
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            Text(
                text = "Available Senior High School Tracks",
                style = MaterialTheme.typography.subtitle1.copy(fontWeight = FontWeight.Bold, color = PrimaryBlue),
                modifier = Modifier.padding(bottom = 16.dp)
            )
            
            Text(
                text = "Select the tracks that are available in your school. These will appear in dropdown menus for class creation.",
                style = MaterialTheme.typography.body2.copy(color = MaterialTheme.colors.onBackground.copy(alpha = 0.6f)),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val allPossibleTracks = listOf(
                "ABM", "HUMSS", "STEM", "GAS", // Academic
                "AFA", "HE", "IA", "ICT",      // TVL
                "Arts and Design", "Sports"    // Others
            )

            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(allPossibleTracks) { track ->
                    val isChecked = enabledTracks.contains(track)
                    Card(
                        elevation = 2.dp,
                        modifier = Modifier.fillMaxWidth().clickable { viewModel.toggleTrack(track, !isChecked) }
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Checkbox(
                                checked = isChecked,
                                onCheckedChange = { viewModel.toggleTrack(track, it) },
                                colors = CheckboxDefaults.colors(checkedColor = PrimaryBlue)
                            )
                            Spacer(Modifier.width(16.dp))
                            Text(
                                text = track,
                                style = MaterialTheme.typography.body1.copy(fontWeight = FontWeight.Medium)
                            )
                        }
                    }
                }
            }
        }
    }
}
