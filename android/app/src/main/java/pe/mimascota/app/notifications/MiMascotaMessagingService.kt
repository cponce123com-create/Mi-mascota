package pe.mimascota.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import pe.mimascota.app.MainActivity

/** Push is opt-in. Firebase will receive messages only if configured by the developer. */
class MiMascotaMessagingService : FirebaseMessagingService() {
    override fun onMessageReceived(message: RemoteMessage) {
        val enabled = getSharedPreferences("mi_mascota_push", MODE_PRIVATE)
            .getBoolean("enabled", false)
        if (!enabled) return
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) !=
                PackageManager.PERMISSION_GRANTED) return

        val manager = getSystemService(NOTIFICATION_SERVICE) as NotificationManager
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            manager.createNotificationChannel(NotificationChannel(
                "community_push", "Novedades de Mi Mascota", NotificationManager.IMPORTANCE_DEFAULT))
        }
        val title = message.notification?.title ?: "Mi Mascota"
        val body = message.notification?.body ?: "Hay novedades en tu comunidad."
        val pending = PendingIntent.getActivity(this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(this, "community_push")
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle(title.take(100))
            .setContentText(body.take(220))
            .setContentIntent(pending)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .setAutoCancel(true)
            .build()
        manager.notify((System.currentTimeMillis() and 0x7FFFFFFF).toInt(), notification)
    }

    // Never register refreshed tokens implicitly: user opt-in controls registration.
    override fun onNewToken(token: String) {
        getSharedPreferences("mi_mascota_push", MODE_PRIVATE)
            .edit().putBoolean("needs_reregister", true).apply()
    }
}
