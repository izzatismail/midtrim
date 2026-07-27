package com.izzatismail.midtrim.domain.repository

sealed interface PurchaseResult {
    data object Success : PurchaseResult
    data object Cancelled : PurchaseResult
    data class Failed(val message: String) : PurchaseResult
}

sealed interface RestoreResult {
    data object Found : RestoreResult
    data object NotFound : RestoreResult
    data class Failed(val message: String) : RestoreResult
}