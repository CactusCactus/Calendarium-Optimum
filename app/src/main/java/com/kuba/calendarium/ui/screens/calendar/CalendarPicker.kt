package com.kuba.calendarium.ui.screens.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kizitonwose.calendar.compose.HorizontalCalendar
import com.kizitonwose.calendar.compose.rememberCalendarState
import com.kizitonwose.calendar.core.CalendarDay
import com.kizitonwose.calendar.core.DayPosition
import com.kizitonwose.calendar.core.firstDayOfWeekFromLocale
import com.kuba.calendarium.ui.common.StandardQuarterSpacer
import kotlinx.coroutines.launch
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

@Composable
fun NewCalendarPicker(
    date: LocalDate,
    onDateSelected: (LocalDate) -> Unit,
    modifier: Modifier = Modifier
) {
    val currentMonth = remember { YearMonth.now() }
    val startMonth = remember { currentMonth.minusMonths(100) }
    val endMonth = remember { currentMonth.plusMonths(100) }
    val firstDayOfWeek = remember { firstDayOfWeekFromLocale() }

    var selectedDate by remember { mutableStateOf(date) }

    val state = rememberCalendarState(
        startMonth = startMonth,
        endMonth = endMonth,
        firstVisibleMonth = currentMonth,
        firstDayOfWeek = firstDayOfWeek
    )

    val composableScope = rememberCoroutineScope()

    HorizontalCalendar(
        state = state,
        dayContent = {
            Day(
                day = it,
                selected = it.date == selectedDate,
                onSelected = {
                    selectedDate = it.date
                    onDateSelected(it.date)

                    if (it.position != DayPosition.MonthDate) {
                        val yearMonth = YearMonth.of(it.date.year, it.date.month)

                        composableScope.launch {
                            state.animateScrollToMonth(yearMonth)
                        }
                    }
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
private fun Day(day: CalendarDay, selected: Boolean, onSelected: (CalendarDay) -> Unit) {
    val selectedColor = MaterialTheme.colorScheme.primaryContainer
    val shape = MaterialTheme.shapes.large

    Box(
        modifier = Modifier
            .aspectRatio(1f)
            .padding(4.dp)
            .background(
                color = if (selected) selectedColor else Color.Transparent,
                shape = shape
            )
            .clip(shape)
            .clickable(onClick = { onSelected(day) }),
        contentAlignment = Alignment.Center
    ) {
        val alpha = if (day.position == DayPosition.MonthDate) 1f else 0.25f
        val textColor = if (selected) MaterialTheme.colorScheme.onPrimaryContainer else
            MaterialTheme.colorScheme.onBackground

        Text(
            text = day.date.dayOfMonth.toString(),
            style = MaterialTheme.typography.bodyLarge,
            color = textColor.copy(alpha = alpha)
        )
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