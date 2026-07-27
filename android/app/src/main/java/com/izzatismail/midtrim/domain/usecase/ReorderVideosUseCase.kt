package com.izzatismail.midtrim.domain.usecase

import com.izzatismail.midtrim.domain.error.ReorderVideosError

class ReorderVideosUseCase {
    fun <T> execute(items: List<T>, newOrder: List<Int>): List<T> {
        if (items.isEmpty()) throw ReorderVideosError.EmptyList
        if (newOrder.size != items.size || newOrder.toSet() != (0 until items.size).toSet()) {
            throw ReorderVideosError.InvalidIndices
        }
        return newOrder.map { items[it] }
    }
}