package com.gloster.yaplan.ui.list

import com.gloster.yaplan.data.Task
import com.gloster.yaplan.ui.toLocalDateTime
import java.time.ZoneId

enum class TaskTab(val label: String) {
    ALL("Tümü"),
    OPEN("Yapılacak"),
    DONE("Tamamlanan"),
}

data class TaskFilter(
    val tab: TaskTab = TaskTab.ALL,
    val categoryId: Long? = null,
)

enum class SectionKind(val label: String) {
    OVERDUE("Gecikenler"),
    TODAY("Bugün"),
    TOMORROW("Yarın"),
    LATER("Daha sonra"),
    NO_DATE("Tarihsiz"),
    DONE("Tamamlanan"),
}

data class TaskSection(val kind: SectionKind, val tasks: List<Task>)

data class DailyProgress(val done: Int, val total: Int) {
    val left: Int get() = total - done
}

/**
 * [pinnedId] is the task that was just completed: it keeps its open position for a moment
 * so it doesn't jump away while the celebration is on screen.
 */
fun applyFilter(tasks: List<Task>, filter: TaskFilter, pinnedId: Long? = null): List<Task> {
    fun Task.settled() = isDone && id != pinnedId
    return tasks
        .filter {
            when (filter.tab) {
                TaskTab.ALL -> true
                TaskTab.OPEN -> !it.settled()
                TaskTab.DONE -> it.isDone
            }
        }
        .filter { filter.categoryId == null || it.categoryId == filter.categoryId }
        .sortedWith(
            compareBy<Task> { it.settled() }
                .thenBy(nullsLast()) { it.dueAt }
                .thenBy { it.priority.ordinal }
                .thenByDescending { it.createdAt },
        )
}

fun sectionOf(task: Task, now: Long, zone: ZoneId, pinnedId: Long? = null): SectionKind {
    if (task.isDone && task.id != pinnedId) return SectionKind.DONE
    val dueAt = task.dueAt ?: return SectionKind.NO_DATE
    if (dueAt < now) return SectionKind.OVERDUE
    val today = now.toLocalDateTime(zone).toLocalDate()
    val dueDate = dueAt.toLocalDateTime(zone).toLocalDate()
    return when {
        !dueDate.isAfter(today) -> SectionKind.TODAY
        dueDate == today.plusDays(1) -> SectionKind.TOMORROW
        else -> SectionKind.LATER
    }
}

/** Keeps the order of [sorted] inside each section. */
fun groupIntoSections(sorted: List<Task>, now: Long, zone: ZoneId, pinnedId: Long? = null): List<TaskSection> {
    val byKind = sorted.groupBy { sectionOf(it, now, zone, pinnedId) }
    return SectionKind.entries.mapNotNull { kind -> byKind[kind]?.let { TaskSection(kind, it) } }
}

fun dailyProgress(tasks: List<Task>, now: Long, zone: ZoneId): DailyProgress {
    val today = now.toLocalDateTime(zone).toLocalDate()
    val todays = tasks.filter { it.dueAt?.toLocalDateTime(zone)?.toLocalDate() == today }
    return DailyProgress(done = todays.count { it.isDone }, total = todays.size)
}

fun progressMessage(progress: DailyProgress): String = with(progress) {
    when {
        total == 0 -> "Bugün için görev yok."
        left == 0 -> "Hepsini yaptın lan!"
        done == 0 -> "Hadi lan, ilkini yap!"
        left == 1 -> "Son bir tane. Bitir lan!"
        done * 2 >= total -> "Yarıyı geçtin! $left tane kaldı."
        else -> "$left tane kaldı. Yap lan!"
    }
}
