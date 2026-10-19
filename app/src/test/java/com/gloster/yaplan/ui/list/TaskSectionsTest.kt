package com.gloster.yaplan.ui.list

import com.gloster.yaplan.data.Task
import java.time.LocalDateTime
import java.time.ZoneOffset
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskSectionsTest {
    private val zone = ZoneOffset.UTC
    private val now = at(19, 10, 0)

    private fun at(day: Int, hour: Int, minute: Int) =
        LocalDateTime.of(2026, 10, day, hour, minute).toInstant(zone).toEpochMilli()

    private fun task(id: Long, dueAt: Long?, done: Boolean = false) =
        Task(id = id, title = "Görev $id", dueAt = dueAt, isDone = done)

    @Test
    fun putsEachTaskInTheRightSection() {
        assertEquals(SectionKind.OVERDUE, sectionOf(task(1, at(18, 18, 0)), now, zone))
        assertEquals(SectionKind.OVERDUE, sectionOf(task(2, at(19, 8, 0)), now, zone))
        assertEquals(SectionKind.TODAY, sectionOf(task(3, at(19, 14, 0)), now, zone))
        assertEquals(SectionKind.TOMORROW, sectionOf(task(4, at(20, 9, 0)), now, zone))
        assertEquals(SectionKind.LATER, sectionOf(task(5, at(22, 9, 0)), now, zone))
        assertEquals(SectionKind.NO_DATE, sectionOf(task(6, null), now, zone))
        assertEquals(SectionKind.DONE, sectionOf(task(7, at(18, 18, 0), done = true), now, zone))
    }

    @Test
    fun pinnedDoneTaskKeepsItsSection() {
        assertEquals(SectionKind.OVERDUE, sectionOf(task(1, at(18, 18, 0), done = true), now, zone, pinnedId = 1))
        assertEquals(SectionKind.TODAY, sectionOf(task(2, at(19, 14, 0), done = true), now, zone, pinnedId = 2))
    }

    @Test
    fun groupsInSectionOrderAndKeepsTaskOrder() {
        val tasks = listOf(
            task(3, at(19, 14, 0)),
            task(4, at(19, 20, 0)),
            task(1, at(18, 18, 0)),
            task(6, null),
            task(7, null, done = true),
        )
        val sections = groupIntoSections(tasks, now, zone)
        assertEquals(
            listOf(SectionKind.OVERDUE, SectionKind.TODAY, SectionKind.NO_DATE, SectionKind.DONE),
            sections.map { it.kind },
        )
        assertEquals(listOf(3L, 4L), sections[1].tasks.map { it.id })
    }

    @Test
    fun dailyProgressCountsOnlyTodaysTasks() {
        val tasks = listOf(
            task(1, at(19, 8, 0), done = true),
            task(2, at(19, 14, 0)),
            task(3, at(18, 18, 0), done = true),
            task(4, null),
        )
        assertEquals(DailyProgress(done = 1, total = 2), dailyProgress(tasks, now, zone))
    }

    @Test
    fun progressMessages() {
        assertEquals("Bugün için görev yok.", progressMessage(DailyProgress(0, 0)))
        assertEquals("Hadi lan, ilkini yap!", progressMessage(DailyProgress(0, 4)))
        assertEquals("3 tane kaldı. Yap lan!", progressMessage(DailyProgress(1, 4)))
        assertEquals("Yarıyı geçtin! 3 tane kaldı.", progressMessage(DailyProgress(3, 6)))
        assertEquals("Son bir tane. Bitir lan!", progressMessage(DailyProgress(3, 4)))
        assertEquals("Hepsini yaptın lan!", progressMessage(DailyProgress(4, 4)))
    }
}
