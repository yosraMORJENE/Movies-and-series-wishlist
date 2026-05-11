package tn.poly.movies_wishlist.notifications

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import java.util.Calendar

object NotificationScheduler {

    fun scheduleEpisodeNotification(
        context: Context,
        seriesId: Long,
        seriesTitle: String,
        releaseDay: String
    ) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val calendar = Calendar.getInstance()
        val currentDay = calendar.get(Calendar.DAY_OF_WEEK)
        val targetDay = getDayOfWeek(releaseDay)

        // Calculate days until target day
        var daysUntil = targetDay - currentDay
        if (daysUntil <= 0) {
            daysUntil += 7
        }

        // Set time to 9 AM on the target day
        calendar.add(Calendar.DAY_OF_YEAR, daysUntil)
        calendar.set(Calendar.HOUR_OF_DAY, 9)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)

        val intent = Intent(context, EpisodeReleaseReceiver::class.java).apply {
            putExtra("series_title", seriesTitle)
            putExtra("series_id", seriesId)
            putExtra("release_day", releaseDay)
            action = "tn.poly.movies_wishlist.EPISODE_RELEASE"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            context,
            seriesId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            // Handle case where permission is not granted
            e.printStackTrace()
        }
    }

    fun cancelEpisodeNotification(context: Context, seriesId: Long) {
        val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager

        val intent = Intent(context, EpisodeReleaseReceiver::class.java)
        intent.action = "tn.poly.movies_wishlist.EPISODE_RELEASE"
        val pendingIntent = PendingIntent.getBroadcast(
            context,
            seriesId.toInt(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        alarmManager.cancel(pendingIntent)
    }

    private fun getDayOfWeek(day: String): Int {
        return when (day.lowercase()) {
            "monday" -> Calendar.MONDAY
            "tuesday" -> Calendar.TUESDAY
            "wednesday" -> Calendar.WEDNESDAY
            "thursday" -> Calendar.THURSDAY
            "friday" -> Calendar.FRIDAY
            "saturday" -> Calendar.SATURDAY
            "sunday" -> Calendar.SUNDAY
            else -> Calendar.SUNDAY
        }
    }
}
