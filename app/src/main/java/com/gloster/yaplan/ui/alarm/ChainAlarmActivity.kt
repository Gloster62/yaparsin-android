package com.gloster.yaplan.ui.alarm

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Alarm
import androidx.compose.material3.Icon
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.lifecycleScope
import com.gloster.yaplan.YapLanApp
import com.gloster.yaplan.data.currentStreak
import com.gloster.yaplan.data.nextMidnightAt
import com.gloster.yaplan.reminder.ChainAlarm
import com.gloster.yaplan.reminder.ReminderScheduler
import com.gloster.yaplan.ui.components.PrimaryButton
import com.gloster.yaplan.ui.components.SecondaryButton
import com.gloster.yaplan.ui.formatDuration
import com.gloster.yaplan.ui.theme.Yap
import com.gloster.yaplan.ui.theme.YapLanTheme
import java.time.LocalDate
import java.time.ZoneId
import kotlinx.coroutines.launch

private data class AlarmInfo(val name: String, val streak: Int, val untilMidnight: Long)

/** The full-screen face of [ChainAlarm]; every way out stops the ringing. */
class ChainAlarmActivity : ComponentActivity() {
    private var info by mutableStateOf<AlarmInfo?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showOverLockScreen()
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.dark(Color.TRANSPARENT),
        )
        load()
        setContent {
            YapLanTheme {
                BackHandler(onBack = ::dismiss)
                info?.let { AlarmScreen(it, onDone = ::markDone, onSnooze = ::snooze, onDismiss = ::dismiss) }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        load()
    }

    private val habitId: Long get() = intent.getLongExtra(EXTRA_HABIT_ID, -1)
    private val app: YapLanApp get() = application as YapLanApp

    private fun load() {
        val id = habitId
        lifecycleScope.launch {
            val habit = app.habits.getHabit(id) ?: return@launch dismiss()
            val now = System.currentTimeMillis()
            info = AlarmInfo(
                name = habit.name,
                streak = currentStreak(app.habits.doneDays(id), LocalDate.now()),
                untilMidnight = nextMidnightAt(now, ZoneId.systemDefault()) - now,
            )
        }
    }

    private fun markDone() {
        val id = habitId
        app.scope.launch { app.habits.setDone(id, LocalDate.now(), true) }
        dismiss()
    }

    private fun snooze() {
        app.scheduler.snoozeHabit(habitId, System.currentTimeMillis() + ReminderScheduler.SNOOZE_MILLIS)
        dismiss()
    }

    private fun dismiss() {
        ChainAlarm.stop(this, habitId)
        finish()
    }

    private fun showOverLockScreen() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O_MR1) {
            setShowWhenLocked(true)
            setTurnScreenOn(true)
        } else {
            @Suppress("DEPRECATION")
            window.addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED or WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON)
        }
    }

    companion object {
        private const val EXTRA_HABIT_ID = "habit_id"

        fun intent(context: Context, habitId: Long): Intent =
            Intent(context, ChainAlarmActivity::class.java)
                .putExtra(EXTRA_HABIT_ID, habitId)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_NO_USER_ACTION)
    }
}

@Composable
private fun AlarmScreen(info: AlarmInfo, onDone: () -> Unit, onSnooze: () -> Unit, onDismiss: () -> Unit) {
    val pulse by rememberInfiniteTransition(label = "pulse").animateFloat(
        initialValue = 1f,
        targetValue = 1.12f,
        animationSpec = infiniteRepeatable(tween(700, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "scale",
    )
    Surface(Modifier.fillMaxSize(), color = Yap.Background, contentColor = Yap.Text) {
        Column(
            Modifier
                .systemBarsPadding()
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Column(
                Modifier.weight(1f),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(14.dp, Alignment.CenterVertically),
            ) {
                Box(
                    Modifier
                        .size(112.dp)
                        .scale(pulse)
                        .background(Yap.Accent, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Icon(Icons.Outlined.Alarm, contentDescription = null, tint = Yap.OnAccent, modifier = Modifier.size(56.dp))
                }
                Text("ZİNCİR ALARMI", color = Yap.Accent, fontSize = 13.sp, fontWeight = FontWeight.Bold, letterSpacing = 1.5.sp)
                Text(
                    info.name,
                    modifier = Modifier.semantics { heading() },
                    fontSize = 30.sp,
                    lineHeight = 36.sp,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                )
                Text(
                    if (info.streak > 0) "${info.streak} günlük zincirin seni bekliyor" else "Bugün henüz yapılmadı",
                    fontSize = 17.sp,
                    color = Yap.TextSecondary,
                    textAlign = TextAlign.Center,
                )
                Text(
                    "Gece yarısına ${formatDuration(info.untilMidnight)}",
                    fontSize = 14.sp,
                    color = Yap.TextMuted,
                )
            }
            Column(Modifier.fillMaxWidth(), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                PrimaryButton("Yaptım", onClick = onDone, modifier = Modifier.fillMaxWidth())
                SecondaryButton("10 dk ertele", onClick = onSnooze, modifier = Modifier.fillMaxWidth())
                TextButton(onClick = onDismiss, modifier = Modifier.align(Alignment.CenterHorizontally)) {
                    Text("Kapat", color = Yap.TextMuted, fontSize = 15.sp)
                }
            }
        }
    }
}
