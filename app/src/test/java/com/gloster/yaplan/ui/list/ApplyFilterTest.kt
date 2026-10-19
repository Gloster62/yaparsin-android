package com.gloster.yaplan.ui.list

import com.gloster.yaplan.data.Priority
import com.gloster.yaplan.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class ApplyFilterTest {
    private val milk = Task(id = 1, title = "Süt al", categoryId = 2, dueAt = 2_000, createdAt = 1)
    private val homework = Task(id = 2, title = "Ödev yap", note = "matematik", categoryId = 3, dueAt = 1_000, createdAt = 2)
    private val call = Task(id = 3, title = "Annemi ara", isDone = true, createdAt = 3)
    private val urgent = Task(id = 4, title = "Fatura öde", priority = Priority.HIGH, createdAt = 4)
    private val relaxed = Task(id = 5, title = "Kitap oku", priority = Priority.LOW, createdAt = 5)
    private val all = listOf(milk, homework, call, urgent, relaxed)

    private fun ids(filter: TaskFilter, pinnedId: Long? = null) = applyFilter(all, filter, pinnedId).map { it.id }

    @Test
    fun sortsOpenFirstThenByDueDateThenPriority() {
        assertEquals(listOf(2L, 1L, 4L, 5L, 3L), ids(TaskFilter()))
    }

    @Test
    fun filtersByTab() {
        assertEquals(listOf(2L, 1L, 4L, 5L), ids(TaskFilter(tab = TaskTab.OPEN)))
        assertEquals(listOf(3L), ids(TaskFilter(tab = TaskTab.DONE)))
    }

    @Test
    fun filtersByCategory() {
        assertEquals(listOf(1L), ids(TaskFilter(categoryId = 2)))
        assertEquals(listOf(2L), ids(TaskFilter(tab = TaskTab.OPEN, categoryId = 3)))
    }

    @Test
    fun justCompletedTaskStaysInOpenTabWhilePinned() {
        val done = all.map { if (it.id == 2L) it.copy(isDone = true) else it }
        assertEquals(listOf(2L, 1L, 4L, 5L), applyFilter(done, TaskFilter(tab = TaskTab.OPEN), pinnedId = 2).map { it.id })
        assertEquals(listOf(1L, 4L, 5L), applyFilter(done, TaskFilter(tab = TaskTab.OPEN)).map { it.id })
    }
}
