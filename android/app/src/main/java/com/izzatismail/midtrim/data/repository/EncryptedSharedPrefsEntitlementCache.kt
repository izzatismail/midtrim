package com.izzatismail.midtrim.data.repository

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.izzatismail.midtrim.domain.repository.EntitlementCacheWriter

class EncryptedSharedPrefsEntitlementCache(
    context: Context
) : EntitlementCacheWriter {

    private val prefs: SharedPreferences = run {
        val masterKey = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            PREFS_FILE,
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override var isPurchased: Boolean
        get() = prefs.getBoolean(KEY_IS_PURCHASED, false)
        set(value) = prefs.edit().putBoolean(KEY_IS_PURCHASED, value).apply()

    override var productId: String?
        get() = prefs.getString(KEY_PRODUCT_ID, null)
        set(value) = prefs.edit().putString(KEY_PRODUCT_ID, value).apply()

    override var lastVerifiedAt: Long?
        get() = if (prefs.contains(KEY_LAST_VERIFIED_AT)) prefs.getLong(KEY_LAST_VERIFIED_AT, 0L) else null
        set(value) {
            if (value != null) {
                prefs.edit().putLong(KEY_LAST_VERIFIED_AT, value).apply()
            } else {
                prefs.edit().remove(KEY_LAST_VERIFIED_AT).apply()
            }
        }

    private companion object {
        const val PREFS_FILE = "entitlement_prefs"
        const val KEY_IS_PURCHASED = "is_purchased"
        const val KEY_PRODUCT_ID = "product_id"
        const val KEY_LAST_VERIFIED_AT = "last_verified_at"
    }
}