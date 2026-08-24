package com.hostelhub.app.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object Formatters {
    fun formatCurrency(amount: Double): String {
        return "₹" + String.format(Locale.ENGLISH, "%,.2f", amount)
    }

    fun formatCurrencyNoDecimals(amount: Double): String {
        return "₹" + String.format(Locale.ENGLISH, "%,.0f", amount)
    }

    fun formatDate(timestamp: Long, pattern: String = "dd MMM yyyy"): String {
        return try {
            val sdf = SimpleDateFormat(pattern, Locale.getDefault())
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            ""
        }
    }

    fun formatDateTime(timestamp: Long): String {
        return formatDate(timestamp, "dd MMM yyyy, hh:mm a")
    }
}

object Constants {
    const val DEFAULT_HOSTEL_ID = "hostel_001"
    const val GLOBAL_CAMPUS = "GLOBAL_CAMPUS"
}
