package com.izzatismail.midtrim.domain.repository

interface EntitlementCacheReader {
    val isPurchased: Boolean
    val productId: String?
    val lastVerifiedAt: Long?
}

interface EntitlementCacheWriter : EntitlementCacheReader {
    override var isPurchased: Boolean
    override var productId: String?
    override var lastVerifiedAt: Long?
}