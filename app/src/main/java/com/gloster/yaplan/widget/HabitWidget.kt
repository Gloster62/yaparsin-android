package com.gloster.yaplan.widget

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.LocalSize
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.GlanceAppWidgetManager
import androidx.glance.appwidget.SizeMode
import androidx.glance.appwidget.action.actionStartActivity
import androidx.glance.appwidget.provideContent
import androidx.glance.appwidget.state.updateAppWidgetState
import androidx.glance.background
import androidx.glance.currentState
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.RowScope
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxHeight
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import com.gloster.yaplan.DeepLinks
import com.gloster.yaplan.R
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.ChainMood
import com.gloster.yaplan.data.ChainPart
import com.gloster.yaplan.data.DayStatus
import com.gloster.yaplan.data.Habit
import com.gloster.yaplan.data.chainMood
import com.gloster.yaplan.data.chainPart
import com.gloster.yaplan.data.currentStreak
import com.gloster.yaplan.data.dayStatus
import com.gloster.yaplan.data.monthWeeks
import com.gloster.yaplan.data.startDay
import com.gloster.yaplan.ui.formatMonth
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.weekdayInitials
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.YearMonth
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf

private val HABIT_ID = longPreferencesKey("habit_id")

private sealed interface WidgetContent {
    data object Loading : WidgetContent
    data object Unassigned : WidgetContent
    data object Deleted : WidgetContent
    data class Ready(val habit: Habit, val done: Set<LocalDate>) : WidgetContent
}

/** One habit's chain for the current month; tapping opens that habit in the app. */
class HabitWidget : GlanceAppWidget() {
    override val sizeMode: SizeMode = SizeMode.Exact

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        val habits = (context.applicationContext as YapLanApp).habits
        val configure = Intent(context, HabitWidgetConfigActivity::class.java)
            .putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, GlanceAppWidgetManager(context).getAppWidgetId(id))
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)

        provideContent {
            val habitId = currentState(HABIT_ID)
            val content by remember(habitId) {
                if (habitId == null) {
                    flowOf(WidgetContent.Unassigned)
                } else {
                    combine(habits.observeHabit(habitId), habits.observeDoneDays(habitId)) { habit, done ->
                        if (habit == null) WidgetContent.Deleted else WidgetContent.Ready(habit, done)
                    }
                }
            }.collectAsState(WidgetContent.Loading)
            HabitWidgetContent(content, LocalDateTime.now(), configure)
        }
    }

    companion object {
        suspend fun assign(context: Context, appWidgetId: Int, habitId: Long) {
            val glanceId = GlanceAppWidgetManager(context).getGlanceIdBy(appWidgetId)
            updateAppWidgetState(context, glanceId) { it[HABIT_ID] = habitId }
            HabitWidget().update(context, glanceId)
        }
    }
}

@Composable
private fun HabitWidgetContent(content: WidgetContent, now: LocalDateTime, configure: Intent) {
    val context = LocalContext.current
    val open = when (content) {
        is WidgetContent.Ready -> DeepLinks.intent(context, DeepLinks.habit(content.habit.id))
        else -> configure
    }
    val background = when ((content as? WidgetContent.Ready)?.let { chainMood(it.done, now) }) {
        ChainMood.DONE_TODAY -> R.drawable.widget_background_done
        ChainMood.LAST_CALL -> R.drawable.widget_background_danger
        else -> R.drawable.widget_background
    }
    Box(
        GlanceModifier
            .fillMaxSize()
            .background(ImageProvider(background))
            .padding(12.dp)
            .clickable(actionStartActivity(open)),
        contentAlignment = Alignment.Center,
    ) {
        when (content) {
            WidgetContent.Loading -> Unit
            WidgetContent.Unassigned -> WidgetMessage("Alışkanlık seçmek için dokun")
            WidgetContent.Deleted -> WidgetMessage("Bu alışkanlık silinmiş.\nBaşka birini seçmek için dokun")
            is WidgetContent.Ready -> MonthChain(content.habit, content.done, now)
        }
    }
}

@Composable
private fun MonthChain(habit: Habit, done: Set<LocalDate>, now: LocalDateTime) {
    val today = now.toLocalDate()
    val month = YearMonth.from(today)
    val weeks = monthWeeks(month)
    val streak = currentStreak(done, today)
    val mood = chainMood(done, now)
    val startDay = habit.startDay()
    val rowHeight = (LocalSize.current.height - 84.dp) / weeks.size
    val bandHeight = (rowHeight - 4.dp).coerceIn(16.dp, 34.dp)
    val dayFontSize = if (rowHeight < 30.dp) 11.sp else 13.sp

    Column(GlanceModifier.fillMaxSize()) {
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                habit.name,
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = color(Yap.Text), fontSize = 15.sp, fontWeight = FontWeight.Bold),
                maxLines = 1,
            )
            Text(
                if (streak > 0) "$streak gün" else "Zincir yok",
                style = TextStyle(
                    color = color(
                        when {
                            mood == ChainMood.LAST_CALL -> Yap.Danger
                            streak > 0 -> Yap.Accent
                            else -> Yap.TextMuted
                        },
                    ),
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold,
                ),
            )
        }
        Row(GlanceModifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
            Text(
                moodMessage(mood),
                modifier = GlanceModifier.defaultWeight(),
                style = TextStyle(color = color(moodColor(mood)), fontSize = 12.sp, fontWeight = FontWeight.Medium),
                maxLines = 1,
            )
            Text(formatMonth(month), style = TextStyle(color = color(Yap.TextFaint), fontSize = 11.sp))
        }
        Spacer(GlanceModifier.height(6.dp))
        Row(GlanceModifier.fillMaxWidth()) {
            weekdayInitials.forEach {
                Text(
                    it,
                    modifier = GlanceModifier.defaultWeight(),
                    style = TextStyle(color = color(Yap.TextFaint), fontSize = 11.sp, textAlign = TextAlign.Center),
                )
            }
        }
        Spacer(GlanceModifier.height(2.dp))
        Column(GlanceModifier.fillMaxWidth().defaultWeight()) {
            weeks.forEach { week ->
                Row(GlanceModifier.fillMaxWidth().defaultWeight()) {
                    week.forEach { day ->
                        DayCell(day, day?.let { dayStatus(it, done, today, startDay) }, done, bandHeight, dayFontSize)
                    }
                }
            }
        }
    }
}

@Composable
private fun RowScope.DayCell(
    day: LocalDate?,
    status: DayStatus?,
    done: Set<LocalDate>,
    bandHeight: Dp,
    fontSize: TextUnit,
) {
    Box(GlanceModifier.defaultWeight().fillMaxHeight(), contentAlignment = Alignment.Center) {
        if (day == null || status == null) return@Box
        val background = when (status) {
            DayStatus.DONE -> when (chainPart(day, done)) {
                ChainPart.SINGLE -> R.drawable.chain_single
                ChainPart.START -> R.drawable.chain_start
                ChainPart.MIDDLE -> R.drawable.chain_middle
                ChainPart.END -> R.drawable.chain_end
            }
            DayStatus.TODAY -> R.drawable.widget_today
            DayStatus.MISSED -> R.drawable.widget_missed
            DayStatus.FUTURE, DayStatus.NOT_STARTED -> null
        }
        val textColor = when (status) {
            DayStatus.DONE -> Yap.OnAccent
            DayStatus.TODAY -> Yap.Accent
            DayStatus.MISSED -> Yap.TextFaint
            DayStatus.FUTURE, DayStatus.NOT_STARTED -> Yap.EmptyIcon
        }
        Box(
            GlanceModifier
                .fillMaxWidth()
                .height(bandHeight)
                .then(if (background != null) GlanceModifier.background(ImageProvider(background)) else GlanceModifier),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                "${day.dayOfMonth}",
                style = TextStyle(
                    color = color(textColor),
                    fontSize = fontSize,
                    fontWeight = if (status == DayStatus.DONE || status == DayStatus.TODAY) FontWeight.Bold else FontWeight.Normal,
                    textAlign = TextAlign.Center,
                ),
            )
        }
    }
}

private fun moodMessage(mood: ChainMood): String = when (mood) {
    ChainMood.DONE_TODAY -> "Bugün tamam, zincir sağlam"
    ChainMood.FRESH -> "İlk halkayı bugün tak"
    ChainMood.BROKEN -> "Zincir kırıldı, bugün yeniden başla"
    ChainMood.CALM -> "Bugün henüz yapılmadı"
    ChainMood.NUDGE -> "Akşam oluyor, zinciri unutma"
    ChainMood.URGENT -> "Zincir seni bekliyor!"
    ChainMood.LAST_CALL -> "Son şans! Gece yarısı kırılır"
}

private fun moodColor(mood: ChainMood): Color = when (mood) {
    ChainMood.DONE_TODAY, ChainMood.URGENT -> Yap.Accent
    ChainMood.LAST_CALL -> Yap.Danger
    ChainMood.NUDGE -> Yap.Text
    ChainMood.FRESH, ChainMood.BROKEN, ChainMood.CALM -> Yap.TextMuted
}
