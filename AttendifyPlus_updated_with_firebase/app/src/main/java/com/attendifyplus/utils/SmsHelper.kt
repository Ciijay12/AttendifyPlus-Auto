package com.attendifyplus.utils

import android.content.Context
import android.os.Build
import android.telephony.SmsManager
import timber.log.Timber

/**
 * Single-utility class for managing Parent SMS Notifications.
 * Feature is currently LOCKED via [IS_FEATURE_ENABLED].
 */
object SmsHelper {
    private const val IS_FEATURE_ENABLED = false // Toggle to true to enable SMS

    fun notifyParent(
        context: Context,
        studentName: String, 
        parentPhoneNumber: String, 
        status: String, 
        className: String = "Advisory Class"
    ) {
        if (!IS_FEATURE_ENABLED) {
            Timber.i("[SMS LOCKED] Would have sent to $parentPhoneNumber: Your child $studentName is $status in $className.")
            return
        }

        try {
            val message = "AttendifyPlus: Your child $studentName is marked $status in $className."
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }
            smsManager.sendTextMessage(parentPhoneNumber, null, message, null, null)
            Timber.d("SMS sent to $parentPhoneNumber")
        } catch (e: Exception) {
            Timber.e(e, "Failed to send SMS")
        }
    }
}
