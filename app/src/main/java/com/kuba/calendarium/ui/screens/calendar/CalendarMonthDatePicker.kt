package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.CalendarState
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.WeekCalendar
import com.kizitonwose.calendar.compose.weekcalendar.WeekCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.WeekDay
import com.kuba.calendarium.ui.common.StandardQuarterSpacer
import com.kuba.calendarium.ui.common.standardQuarterPadding
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private const val DAY_TEXT_ALPHA_OTHER_MONTH = 0.25f

private const val WEEK_BOX_ASPECT_RATIO = 0.8f

@Composable
fun CalendarMonthDatePicker(
    state: CalendarState,
    initialSelectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventsMap: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember(initialSelectedDate) { mutableStateOf(initialSelectedDate) }

    LaunchedEffect(selectedDate.month) {
        state.animateScrollToMonth(YearMonth.of(selectedDate.year, selectedDate.month))
    }

    HorizontalCalendar(
        state = state,
        dayContent = {
            DayBox(
                day = it,
                selected = it.date == selectedDate,
                eventCount = eventsMap[it.date] ?: 0,
                onSelected = {
                    selectedDate = it.date
                    onDateSelected(it.date)
                }
            )
        },
        monthHeader = {
            val daysOfWeek = it.weekDays.first().map { it.date.dayOfWeek }
            MonthHeader(daysOfWeek)
            StandardQuarterSpacer()
        },
        modifier = modifier
    )
}

@Composable
fun CalendarWeekDatePicker(
    state: WeekCalendarState,
    initialSelectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    eventsMap: Map<LocalDate, Int>,
    modifier: Modifier = Modifier
) {
    var selectedDate by remember(initialSelectedDate) { mutableStateOf(initialSelectedDate) }

    LaunchedEffect(selectedDate.dayOfWeek) {
        state.animateScrollToWeek(selectedDate)
    }

    WeekCalendar(
        state = state,
        dayContent = {
            WeekDayBox(
                day = it,
                selected = it.date == selectedDate,
                eventCount = eventsMap[it.date] ?: 0,
                onSelected = {
                    selectedDate = it.date
                    onDateSelected(it.date)
                }
            )
        },
        modifier = modifier
    )
}

@Composable
private fun WeekDayBox(
    day: WeekDay,
    selected: Boolean,
    eventCount: Int,
    onSelected: (WeekDay) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val shape = MaterialTheme.shapes.large

    val todayModifier = if (day.date == LocalDate.now()) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = shape
        )
    } else Modifier

    Column(
        modifier = Modifier
            .aspectRatio(WEEK_BOX_ASPECT_RATIO)
            .padding(standardQuarterPadding)
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = shape
            )
            .then(todayModifier)
            .clip(shape)
            .clickable(onClick = { onSelected(day) })
            .padding(standardQuarterPadding),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val textColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else
            MaterialTheme.colorScheme.onBackground
        Text(
            text = day.date.dayOfWeek.displayText(),
            style = MaterialTheme.typography.bodyMedium,
            color = textColor
        )

        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor
        )

        if (eventCount > 0) {
            Box(
                modifier = Modifier
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun DayBox(
    day: CalendarDay,
    selected: Boolean,
    eventCount: Int,
    onSelected: (CalendarDay) -> Unit
) {
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val shape = MaterialTheme.shapes.large

    val todayModifier = if (day.date == LocalDate.now()) {
        Modifier.border(
            width = 1.dp,
            color = MaterialTheme.colorScheme.primaryContainer,
            shape = CircleShape
        )
    } else Modifier

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(standardQuarterPadding)
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = shape
            )
            .then(todayModifier)
            .clip(shape)
            .clickable(onClick = { onSelected(day) }),
        contentAlignment = Alignment.Center
    ) {
        val alphaText =
            if (day.position == DayPosition.MonthDate) 1f else DAY_TEXT_ALPHA_OTHER_MONTH
        val textColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else
            MaterialTheme.colorScheme.onBackground

        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor.copy(alpha = alphaText)
        )

        if (eventCount > 0) {
            Box(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 4.dp)
                    .size(4.dp)
                    .background(
                        color = MaterialTheme.colorScheme.primaryContainer,
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun MonthHeader(daysOfWeek: List<DayOfWeek>) {
    Row(modifier = Modifier.fillMaxWidth()) {
        for (dayOfWeek in daysOfWeek) {
            Text(
                modifier = Modifier.weight(1f),
                textAlign = TextAlign.Center,
                fontSize = 15.sp,
                text = dayOfWeek.displayText(narrow = true),
                fontWeight = FontWeight.Medium,
            )
        }
    }
}

fun DayOfWeek.displayText(uppercase: Boolean = false, narrow: Boolean = false): String {
    val style = if (narrow) TextStyle.NARROW else TextStyle.SHORT
    return getDisplayName(style, Locale.ENGLISH).let { value ->
        if (uppercase) value.uppercase(Locale.ENGLISH) else value
    }
}
