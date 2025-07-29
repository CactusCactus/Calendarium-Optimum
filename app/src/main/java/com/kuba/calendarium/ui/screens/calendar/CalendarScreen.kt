package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.Card
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.compose.weekcalendar.rememberWeekCalendarState
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kuba.calendarium.R
import com.kuba.calendarium.data.model.EventTasks
import com.kuba.calendarium.data.model.Task
import com.kuba.calendarium.ui.common.AnimatedText
import com.kuba.calendarium.ui.common.CheckboxNoPadding
import com.kuba.calendarium.ui.common.ConfirmDialog
import com.kuba.calendarium.ui.common.ContextMenuBottomSheet
import com.kuba.calendarium.ui.common.LineWithText
import com.kuba.calendarium.ui.common.MonthYearPickerModal
import com.kuba.calendarium.ui.common.StandardHalfSpacer
import com.kuba.calendarium.ui.common.StandardQuarterSpacer
import com.kuba.calendarium.ui.common.TextLabel
import com.kuba.calendarium.ui.common.TextPrimaryBody
import com.kuba.calendarium.ui.common.TextSecondaryBody
import com.kuba.calendarium.ui.common.standardHalfPadding
import com.kuba.calendarium.ui.common.standardIconSize
import com.kuba.calendarium.ui.common.standardPadding
import com.kuba.calendarium.ui.common.taskListMaxHeight
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.CalendarDisplayMode
import com.kuba.calendarium.ui.screens.calendar.CalendarViewModel.UIEvent
import com.kuba.calendarium.util.CALENDAR_MAX_OFFSET_YEARS
import com.kuba.calendarium.util.isSameDay
import com.kuba.calendarium.util.shortDateFormat
import com.kuba.calendarium.util.standardTimeFormat
import com.kuba.calendarium.util.titleDateFormat
import com.kuba.calendarium.util.toLocalizedString
import kotlinx.coroutines.flow.collectLatest
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onNavigateToAddEvent: (selectedDate: LocalDate) -> Unit,
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
                calendarDisplayMode = viewModel.uiState.collectAsState().value.calendarDisplayMode,
                onTitleClicked = { viewModel.onEvent(UIEvent.ShowMonthYearPickerDialog) },
                onSettingsClicked = { viewModel.onEvent(UIEvent.SettingsClicked) },
                onCalendarModeClicked = { viewModel.onEvent(UIEvent.CalendarModeClicked) }
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
            val calendarMode = viewModel.uiState.collectAsState().value.calendarDisplayMode
            val eventsMap = viewModel.eventCountMap.collectAsState().value

            AnimatedVisibility(visible = calendarMode == CalendarDisplayMode.MONTH) {
                CalendarMonthPicker(
                    date = selectedDate,
                    onDateSelected = {
                        viewModel.onEvent(UIEvent.DateSelected(it))
                    },
                    eventsMap = eventsMap,
                    onVisibleDatesChanged = { startDate, endDate ->
                        viewModel.onEvent(UIEvent.VisibleDatesChanged(startDate, endDate))
                    },
                    modifier = Modifier.padding(standardHalfPadding)
                )
            }

            AnimatedVisibility(visible = calendarMode == CalendarDisplayMode.WEEK) {
                CalendarWeekPicker(
                    initialDate = selectedDate,
                    onDateSelected = {
                        viewModel.onEvent(UIEvent.DateSelected(it))
                    },
                    eventsMap = eventsMap,
                    onVisibleDatesChanged = { startDate, endDate ->
                        viewModel.onEvent(UIEvent.VisibleDatesChanged(startDate, endDate))
                    },
                    modifier = Modifier.padding(standardHalfPadding)
                )
            }

            val pagerState = rememberPagerState(
                initialPage = CalendarViewModel.PAGER_INITIAL_OFFSET_DAYS.toInt(),
                pageCount = { CalendarViewModel.PAGER_VIRTUAL_PAGE_COUNT.toInt() }
            )

            LaunchedEffect(pagerState.currentPage, pagerState.isScrollInProgress) {
                if (!pagerState.isScrollInProgress) {
                    val newDateFromPager = viewModel.pageIndexToLocalDate(pagerState.currentPage)

                    if (newDateFromPager != selectedDate) {
                        viewModel.onEvent(UIEvent.DateSelected(newDateFromPager))
                    }
                }
            }

            LaunchedEffect(selectedDate) {
                pagerState.animateScrollToPage(viewModel.localDateToPageIndex(selectedDate).toInt())
            }

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalAlignment = Alignment.Top
            ) { page ->
                EventsList(viewModel, viewModel.pageIndexToLocalDate(page))
            }
        }

        ShowDialogsAndBottomSheets(viewModel)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AppBar(
    title: String,
    calendarDisplayMode: CalendarDisplayMode,
    onTitleClicked: () -> Unit,
    onSettingsClicked: () -> Unit,
    onCalendarModeClicked: () -> Unit
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = MaterialTheme.colorScheme.background
        ),
        title = {
            AnimatedText(
                text = title,
                style = MaterialTheme.typography.displaySmall,
                maxLines = 1,
                modifier = Modifier.clickable(onClick = onTitleClicked)
            )
        },
        navigationIcon = {
            IconButton(onClick = onSettingsClicked) {
                Icon(
                    painter = painterResource(R.drawable.ic_settings_24),
                    contentDescription = "Settings",
                    modifier = Modifier.size(standardIconSize)
                )
            }
        },
        actions = {
            IconButton(onClick = onCalendarModeClicked) {
                val iconRes = if (calendarDisplayMode == CalendarDisplayMode.MONTH) {
                    R.drawable.ic_calendar_week_24
                } else {
                    R.drawable.ic_calendar_month_24
                }

                Icon(
                    painter = painterResource(iconRes),
                    contentDescription = "Calendar Mode",
                    modifier = Modifier.size(standardIconSize)
                )
            }
        }
    )
}

@Composable
private fun EventsList(
    viewModel: CalendarViewModel,
    date: LocalDate,
    modifier: Modifier = Modifier
) {
    val events by remember(date) {
        viewModel.getEventsForDate(date)
    }.collectAsState(initial = emptyList())

    val firstDoneIndex = events.indexOfFirst { it.event.done }

    LazyColumn(
        horizontalAlignment = Alignment.CenterHorizontally,
        contentPadding = PaddingValues(standardPadding),
        modifier = modifier.fillMaxWidth()
    ) {
        if (events.isNotEmpty()) {
            itemsIndexed(
                items = events,
                key = { i: Int, et: EventTasks -> et.event.id }) { i: Int, et: EventTasks ->

                AnimatedVisibility(i == firstDoneIndex) {
                    LineWithText(stringResource(R.string.done))
                }

                StandardHalfSpacer()

                EventRow(
                    eventTasks = et,
                    onLongClick = {
                        viewModel.onEvent(UIEvent.ContextMenuOpen(et.event))
                    },
                    onCheckedChange = {
                        viewModel.onEvent(UIEvent.DoneChanged(et.event, it))
                    },
                    onTaskDoneChanged = { task, done ->
                        viewModel.onEvent(UIEvent.OnTaskDoneChanged(task, done))
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
    eventTasks: EventTasks,
    onLongClick: () -> Unit,
    onCheckedChange: (Boolean) -> Unit,
    onTaskDoneChanged: (Task, Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val event = eventTasks.event
    val tasks = eventTasks.tasks

    Card(
        modifier = modifier.combinedClickable(
            onClick = { /* NO-OP */ },
            onLongClick = onLongClick
        )
    ) {
        Box {
            Column(modifier = Modifier.padding(standardPadding)) {
                if (event.repetition != null) {
                    TextLabel(event.repetition.toLocalizedString(LocalContext.current))
                    StandardQuarterSpacer()
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    CheckboxNoPadding(
                        checked = event.done,
                        onCheckedChange = onCheckedChange
                    )

                    StandardHalfSpacer()

                    TextPrimaryBody(text = event.title, modifier = Modifier.weight(1f))

                    event.time?.let {
                        StandardQuarterSpacer()

                        val timeEnd = if (event.dateEnd != null && event.timeEnd != null) {
                            LocalDateTime.of(event.dateEnd, event.timeEnd)
                        } else {
                            null
                        }

                        TimeDisplay(LocalDateTime.of(event.date, event.time), timeEnd)
                    }
                }

                if (event.description != null && event.description.isNotBlank()) {
                    StandardQuarterSpacer()

                    TextSecondaryBody(text = event.description)
                }

                if (tasks.isNotEmpty()) {
                    StandardHalfSpacer()

                    LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(standardHalfPadding),
                        modifier = Modifier
                            .heightIn(max = taskListMaxHeight)
                            .padding(start = standardHalfPadding)
                    ) {
                        items(tasks) { task ->
                            TaskRow(task = task, onTaskDoneChanged = {
                                onTaskDoneChanged(task, it)
                            })
                        }
                    }
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
private fun TaskRow(task: Task, onTaskDoneChanged: (Boolean) -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        CheckboxNoPadding(
            checked = task.done,
            onCheckedChange = onTaskDoneChanged,
            colors = CheckboxDefaults.colors(
                uncheckedColor = MaterialTheme.colorScheme.onSurfaceVariant
            )
        )

        StandardHalfSpacer()

        TextSecondaryBody(task.title)
    }
}

@Composable
private fun TimeDisplay(
    timeStart: LocalDateTime,
    timeEnd: LocalDateTime?,
    modifier: Modifier = Modifier
) {
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
private fun HourDateText(time: LocalDateTime, showDate: Boolean, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = modifier) {
        Text(
            time.toLocalTime().standardTimeFormat(),
            style = MaterialTheme.typography.bodyLarge,
            maxLines = 1
        )

        if (showDate) {
            Text(
                time.toLocalDate().shortDateFormat(),
                style = MaterialTheme.typography.labelSmall,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun CalendarMonthPicker(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventsMap: Map<LocalDate, Int>,
    onVisibleDatesChanged: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusYears(CALENDAR_MAX_OFFSET_YEARS) }
    val endMonth = remember { currentMonth.plusYears(CALENDAR_MAX_OFFSET_YEARS) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(state.firstVisibleMonth, state.lastVisibleMonth) {
        onVisibleDatesChanged(
            state.firstVisibleMonth.weekDays.first().first().date,
            state.lastVisibleMonth.weekDays.last().last().date
        )
    }

    CalendarMonthDatePicker(
        state = state,
        initialSelectedDate = date,
        onDateSelected = onDateSelected,
        eventsMap = eventsMap,
        modifier = modifier
    )
}

@Composable
private fun CalendarWeekPicker(
    initialDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventsMap: Map<LocalDate, Int>,
    onVisibleDatesChanged: (startDate: LocalDate, endDate: LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { LocalDate.now() }
    val startMonth = remember { currentMonth.minusYears(CALENDAR_MAX_OFFSET_YEARS) }
    val endMonth = remember { currentMonth.plusYears(CALENDAR_MAX_OFFSET_YEARS) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    val state = rememberWeekCalendarState(
        firstVisibleWeekDate = currentMonth,
        startDate = startMonth,
        endDate = endMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    LaunchedEffect(state.startDate, state.endDate) {
        onVisibleDatesChanged(state.startDate, state.endDate)
    }

    CalendarWeekDatePicker(
        state = state,
        initialSelectedDate = initialDate,
        onDateSelected = onDateSelected,
        eventsMap = eventsMap,
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

    if (uiState.datePickerDialogShowing) {
        MonthYearPickerModal(
            initialDate = viewModel.selectedDate.collectAsState().value,
            onDatePicked = {
                viewModel.onEvent(UIEvent.DateSelected(it))
                viewModel.onEvent(UIEvent.DatePickerDialogDismiss)
            },
            onDismissRequest = { viewModel.onEvent(UIEvent.DatePickerDialogDismiss) })
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
