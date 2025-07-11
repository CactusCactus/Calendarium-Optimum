package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.Event
import com.kuba.calendarium.ui.common.AnimatedText
import com.kuba.calendarium.ui.common.CheckboxNoPadding
import com.kuba.calendarium.ui.common.ConfirmDialog
import com.kuba.calendarium.ui.common.ContextMenuBottomSheet
import com.kuba.calendarium.ui.common.LineWithText
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardQuarterSpacer
import com.kuba.calendarium.ui.common.standardHalfPadding
import com.kuba.calendarium.ui.common.standardIconSize
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent
import com.kuba.calendarium.util.isSameDay
import com.kuba.calendarium.util.shortDateFormat
import com.kuba.calendarium.util.standardTimeFormat
import com.kuba.calendarium.util.titleDateFormat
import kotlinx.coroutines.flow.collectLatest
import java.time.Instant
import java.time.YearMonth
import java.time.ZoneOffset

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToAddEvent: (selectedDate: Long) -> Unit,
    onNavigateToEditEvent: (eventId: Long) -> Unit,
    onNavigateToSettings: () -> Unit
) {
    LaunchedEffect(Unit) {
        viewModel.navEvent.collectLatest {
            when (it) {
                is CalendarViewModel.NavEvent.EditEvent -> onNavigateToEditEvent(it.eventId)
                CalendarViewModel.NavEvent.Settings -> onNavigateToSettings()
            }
        }
    }

    Scaffold(
        topBar = {
            AppBar(
                title = viewModel.selectedDate.collectAsState().value.titleDateFormat(),
                onSettingsClicked = { viewModel.onEvent(UIEvent.SettingsClicked) }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                onNavigateToAddEvent(viewModel.selectedDate.value)
            }) {
                Icon(Icons.Filled.Add, contentDescription = "Add new event")
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            val selectedDate = viewModel.selectedDate.collectAsState().value

            CalendarPicker(
                date = selectedDate,
                onDateSelected = {
                    viewModel.onEvent(UIEvent.DateSelected(it))
                },
                modifier = Modifier.padding(standardHalfPadding)
            )

            val pagerState = rememberPagerState(
                initialPage = CalendarViewModel.PAGER_INITIAL_OFFSET_DAYS,
                pageCount = { CalendarViewModel.PAGER_VIRTUAL_PAGE_COUNT }
            )

            LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                if (!pagerState.isScrollInProgress) {
                    val newDateFromPager = viewModel.pageIndexToDateMillis(pagerState.currentPage)

                    if (newDateFromPager != selectedDate) {
                        viewModel.onEvent(UIEvent.DateSelected(newDateFromPager))
                    }
                }
            }

            LaunchedEffect(selectedDate) {
                pagerState.animateScrollToPage(viewModel.dateMillisToPageIndex(selectedDate))
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                EventsList(viewModel, viewModel.pageIndexToDateMillis(page))
            }
        }

        ShowDialogsAndBottomSheets(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(title: String, onSettingsClicked: () -> Unit) {
    TopAppBar(
        title = {
            AnimatedText(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1
            )
        },
        actions = {
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_24),
                    contentDescription = "Settings",
                    modifier = Modifier.size(standardIconSize)
                )
            }
        }
    )
}

@Composable
private fun EventsList(viewModel: CalendarViewModel, date: Long, modifier: Modifier = Modifier) {
    val events by remember(date) {
        viewModel.getEventsForDate(date)
    }.collectAsState(initial = emptyList())

    val firstDoneIndex = events.indexOfFirst { it.done }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(standardPadding),
        modifier = modifier.fillMaxWidth()
    ) {
        if (events.isNotEmpty()) {
            itemsIndexed(
                items = events,
                key = { i: Int, event: Event -> event.id }) { i: Int, event: Event ->

                if (i == firstDoneIndex) {
                    LineWithText(stringResource(R.string.done))
                }

                StandardHalfSpacer()

                EventRow(
                    event = event,
                    onLongClick = {
                        viewModel.onEvent(UIEvent.ContextMenuOpen(event))
                    },
                    onCheckedChange = {
                        viewModel.onEvent(UIEvent.DoneChanged(event, it))
                    },
                    modifier = Modifier
                        .fillMaxSize()
                        .animateItem()
                )

                StandardHalfSpacer()
            }
        } else {
            item {
                Text(
                    text = stringResource(R.string.event_list_empty_label),
                    style = MaterialTheme.typography.bodyLarge
                )
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun EventRow(
    event: Event,
    onLongClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.combinedClickable(
            onClick = { /* NO-OP */ },
            onLongClick = onLongClick
        )
    ) {
        Box {
            Column(modifier = Modifier.padding(standardPadding)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckboxNoPadding(
                        checked = event.done,
                        onCheckedChange = onCheckedChange
                    )

                    StandardQuarterSpacer()

                    Text(
                        text = event.title,
                        style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.weight(1f)
                    )

                    event.time?.let {
                        StandardQuarterSpacer()

                        TimeDisplay(event.time, event.timeEnd)
                    }
                }

                if (event.description.isNotBlank()) {
                    StandardQuarterSpacer()

                    Text(text = event.description, style = MaterialTheme.typography.bodyMedium)
                }
            }

            if (event.done) {
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(MaterialTheme.colorScheme.background.copy(alpha = 0.5f))
                )
            }
        }
    }
}

@Composable
private fun TimeDisplay(timeStart: Long, timeEnd: Long?, modifier: Modifier = Modifier) {
    Row(modifier = modifier, verticalAlignment = Alignment.CenterVertically) {
        val displayDates = timeEnd != null && !timeStart.isSameDay(timeEnd)

        HourDateText(timeStart, displayDates)

        timeEnd?.let {
            Text(text = " — ", style = MaterialTheme.typography.bodyLarge)

            HourDateText(it, displayDates)
        }
    }
}

@Composable
private fun HourDateText(timestamp: Long, showDate: Boolean, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            timestamp.standardTimeFormat(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )

        if (showDate) {
            Text(
                timestamp.shortDateFormat(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalendarPicker(
    date: Long,
    onDateSelected: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val localDate = Instant.ofEpochMilli(date).atZone(ZoneOffset.UTC).toLocalDate()

    CalendarMonthDatePicker(
        state = state,
        initialSelectedDate = localDate,
        onDateSelected = {
            val millis = it.atStartOfDay(ZoneOffset.UTC).toInstant().toEpochMilli()
            onDateSelected(millis)
        },
        modifier = modifier
    )
}

@Composable
private fun ShowDialogsAndBottomSheets(viewModel: CalendarViewModel) {
    val uiState = viewModel.uiState.collectAsState().value

    if (uiState.contextMenuOpen) {
        ContextMenuBottomSheet(
            title = uiState.contextMenuName,
            onDismissRequest = {
                viewModel.onEvent(UIEvent.ContextMenuDismiss)
            },
            onOptionClick = {
                viewModel.onEvent(UIEvent.ContextMenuOptionSelected(it))
            }
        )
    }

    if (uiState.deleteDialogShowing) {
        ShowDeleteDialog(
            onConfirm = { viewModel.onEvent(UIEvent.ContextEventDelete(it)) },
            onDismiss = { viewModel.onEvent(UIEvent.DeleteDialogDismiss) }
        )
    }
}

@Composable
private fun ShowDeleteDialog(onConfirm: (dontAskAgain: Boolean) -> Unit, onDismiss: () -> Unit) {
    var dontAskAgainChecked by remember { mutableStateOf(false) }

    ConfirmDialog(
        onConfirm = { onConfirm(dontAskAgainChecked) },
        onDismiss = onDismiss,
        title = stringResource(R.string.delete_dialog_title),
        content = {
            Column {
                Text(stringResource(R.string.delete_dialog_text))

                StandardQuarterSpacer()

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Checkbox(
                        checked = dontAskAgainChecked,
                        onCheckedChange = { dontAskAgainChecked = it })

                    Text(text = stringResource(R.string.dont_ask_again_label))
                }
            }
        },
        icon = R.drawable.ic_delete_24
    )
}