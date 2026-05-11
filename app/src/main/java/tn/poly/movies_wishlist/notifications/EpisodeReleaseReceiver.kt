package tn.poly.movies_wishlist.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import tn.poly.movies_wishlist.R

class EpisodeReleaseReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val seriesTitle = intent.getStringExtra("series_title") ?: return
        val seriesId = intent.getLongExtra("series_id", -1L)
        val releaseDay = intent.getStringExtra("release_day").orEmpty()
        val channelId = "episode_releases"
        val notificationId = seriesTitle.hashCode()

        // Create notification channel if not already created
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = android.app.NotificationChannel(
                channelId,
                "Episode Releases",
                android.app.NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Notifications for new episode releases"
            }
            val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as android.app.NotificationManager
            manager.createNotificationChannel(channel)
        }

        val notification = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle("New Episode Released!")
            .setContentText("$seriesTitle episode drops today 🎬")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(context).notify(notificationId, notification)

        if (seriesId > 0 && releaseDay.isNotBlank()) {
            NotificationScheduler.scheduleEpisodeNotification(context, seriesId, seriesTitle, releaseDay)
        }
    }
}
