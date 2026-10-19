package com.gloster.yaplan.ui.search

import com.gloster.yaplan.data.Task
import org.junit.Assert.assertEquals
import org.junit.Test

class SearchTasksTest {
    private val milk = Task(id = 1, title = "Süt al", dueAt = 2_000)
    private val homework = Task(id = 2, title = "Ödev yap", note = "matematik", dueAt = 1_000)
    private val report = Task(id = 3, title = "Raporu gönder", isDone = true)
    private val slides = Task(id = 4, title = "Sunum", note = "Rapordaki grafikler")
    private val all = listOf(milk, homework, report, slides)

    private fun ids(query: String) = searchTasks(all, query).map { it.id }

    @Test
    fun emptyQueryFindsNothing() {
        assertEquals(emptyList<Long>(), ids("  "))
    }

    @Test
    fun searchesTitleAndNoteIgnoringCase() {
        assertEquals(listOf(1L), ids("süt"))
        assertEquals(listOf(2L), ids("MATEMATİK"))
        assertEquals(emptyList<Long>(), ids("yok böyle"))
    }

    @Test
    fun openResultsComeBeforeDoneOnes() {
        assertEquals(listOf(4L, 3L), ids("rapor"))
    }
}
