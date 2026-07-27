package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.ReorderVideosError
import org.junit.Assert.assertEquals
import org.junit.Assert.assertThrows
import org.junit.Test

class ReorderVideosUseCaseTest {
    private val useCase = ReorderVideosUseCase()

    @Test
    fun `reorder by index map`() {
        val items = listOf("A", "B", "C", "D")
        val result = useCase.execute(items, listOf(2, 0, 3, 1))
        assertEquals(listOf("C", "A", "D", "B"), result)
    }

    @Test
    fun `reorder to same order`() {
        val items = listOf("A", "B", "C")
        val result = useCase.execute(items, listOf(0, 1, 2))
        assertEquals(listOf("A", "B", "C"), result)
    }

    @Test
    fun `reorder single item`() {
        val items = listOf("A")
        val result = useCase.execute(items, listOf(0))
        assertEquals(listOf("A"), result)
    }

    @Test
    fun `reorder empty list throws`() {
        val items = emptyList<String>()
        assertThrows(ReorderVideosError.EmptyList::class.java) {
            useCase.execute(items, emptyList())
        }
    }

    @Test
    fun `reorder invalid indices throws`() {
        val items = listOf("A", "B", "C")
        assertThrows(ReorderVideosError.InvalidIndices::class.java) {
            useCase.execute(items, listOf(0, 1, 5))
        }
    }

    @Test
    fun `reorder mismatched count throws`() {
        val items = listOf("A", "B", "C")
        assertThrows(ReorderVideosError.InvalidIndices::class.java) {
            useCase.execute(items, listOf(0, 1))
        }
    }
}