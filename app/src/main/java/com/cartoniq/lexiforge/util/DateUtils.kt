package com.cartoniq.lexiforge.util

import com.cartoniq.lexiforge.data.CardEntity
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

/** minSdk 24 has no java.time, so days are plain epoch-day numbers in the local zone. */
object DateUtils {
    private const val DAY_MS = 86_400_000L

    fun epochDay(millis: Long = System.currentTimeMillis()): Long {
        val offset = TimeZone.getDefault().getOffset(millis)
        return Math.floorDiv(millis + offset, DAY_MS)
    }

    fun today(): Long = epochDay()

    fun shortDayLabel(epochDay: Long): String {
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())
        fmt.timeZone = TimeZone.getTimeZone("UTC")
        return fmt.format(Date(epochDay * DAY_MS))
    }

    fun formatInterval(days: Int): String = when {
        days < 30 -> "${days}d"
        days < 365 -> "${days / 30}mo"
        else -> String.format(Locale.US, "%.1fy", days / 365f)
    }

    fun dueLabel(card: CardEntity): String {
        if (card.lastReviewedAt == 0L) return "New"
        val d = card.nextReviewDate - today()
        return when {
            d <= 0L -> "Due now"
            d == 1L -> "Due tomorrow"
            else -> "Due in $d days"
        }
    }
}
