package com.attendifyplus.ui.attendance

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.attendifyplus.data.local.entities.SchoolEventEntity
import com.attendifyplus.ui.components.EventCard
import com.attendifyplus.ui.theme.PrimaryBlue
import kotlinx.coroutines.launch
import org.koin.androidx.compose.getViewModel
import org.koin.core.parameter.parametersOf
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterialApi::class, ExperimentalFoundationApi::class)
@Composable
fun MonthlyEventsScreen(
    navController: NavController,
    year: Int,
    month: Int
) {
    val viewModel: MonthlyEventsViewModel = getViewModel { parametersOf(year, month) }
    val events by viewModel.events.collectAsState()

    val monthName = remember {
        val cal = Calendar.getInstance()
        cal.set(Calendar.YEAR, year)
        cal.set(Calendar.MONTH, month - 1)
        SimpleDateFormat("MMMM yyyy", Locale.getDefault()).format(cal.time)
    }

    val modalSheetState = rememberModalBottomSheetState(
        initialValue = ModalBottomSheetValue.Hidden,
        skipHalfExpanded = true
    )
    val coroutineScope = rememberCoroutineScope()

    ModalBottomSheetLayout(
        sheetState = modalSheetState,
        sheetShape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
        sheetContent = {
            AddEditEventContent(
                onEventAdded = {
                    coroutineScope.launch {
                        modalSheetState.hide()
                    }
                }
            )
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(monthName) },
                    navigationIcon = {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                        }
                    },
                    backgroundColor = Color(0xFFF5F7FA),
                    elevation = 0.dp,
                    modifier = Modifier.statusBarsPadding()
                )
            },
            floatingActionButton = {
                FloatingActionButton(
                    onClick = {
                        coroutineScope.launch {
                            modalSheetState.show()
                        }
                    },
                    backgroundColor = PrimaryBlue,
                    contentColor = Color.White
                ) {
                    Icon(Icons.Default.Add, contentDescription = "Add Event")
                }
            },
            backgroundColor = Color(0xFFF5F7FA)
        ) { paddingValues ->
            if (events.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No events scheduled for this month.", color = Color.Gray)
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues),
                    contentPadding = PaddingValues(16.dp)
                ) {
                    items(events) { event ->
                        EventCard(
                            event = event,
                            onDelete = { viewModel.deleteEvent(it) }
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }
    }
}
