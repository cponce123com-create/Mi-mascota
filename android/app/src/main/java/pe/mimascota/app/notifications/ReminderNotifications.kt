package pe.mimascota.app.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.Data
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.WorkerParameters
import pe.mimascota.app.MainActivity
import pe.mimascota.app.data.ReminderEntity
import java.time.Duration
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.concurrent.TimeUnit

private const val CHANNEL_ID = "pet_reminders"
private fun workName(id: String) = "mi-mascota-reminder-$id"

object ReminderNotifications {
    fun hasPermission(context: Context): Boolean =
        Build.VERSION.SDK_INT < 33 ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
                PackageManager.PERMISSION_GRANTED

    /** Schedule one reminder at 09:00 local time, or on today's remaining time. */
    fun schedule(context: Context, reminder: ReminderEntity) {
        if (reminder.completed) { cancel(context, reminder.id); return }
        val date = runCatching { LocalDate.parse(reminder.dueAt) }.getOrNull() ?: return
        if (date.isBefore(LocalDate.now())) { cancel(context, reminder.id); return }
        val due = date.atTime(9, 0)
        val delay = Duration.between(LocalDateTime.now(), due).toMillis().coerceAtLeast(0L)
        val payload = Data.Builder().putString("id", reminder.id).putString("title", reminder.title).build()
        val request = OneTimeWorkRequestBuilder<ReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setInputData(payload)
            .build()
        WorkManager.getInstance(context).enqueueUniqueWork(
            workName(reminder.id), ExistingWorkPolicy.REPLACE, request)
    }

    fun cancel(context: Context, id: String) {
        WorkManager.getInstance(context).cancelUniqueWork(workName(id))
    }
}

class ReminderWorker(context: Context, parameters: WorkerParameters) : CoroutineWorker(context, parameters) {
    override suspend fun doWork(): Result {
        val id = inputData.getString("id") ?: return Result.failure()
        val title = inputData.getString("title") ?: return Result.failure()
        if (!ReminderNotifications.hasPermission(applicationContext)) return Result.success()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.createNotificationChannel(NotificationChannel(
            CHANNEL_ID, "Recordatorios de mascotas", NotificationManager.IMPORTANCE_DEFAULT))
        val launch = Intent(applicationContext, MainActivity::class.java)
        val pending = PendingIntent.getActivity(applicationContext, 0, launch,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_popup_reminder)
            .setContentTitle("🐾 Mi Mascota")
            .setContentText(title)
            .setContentIntent(pending)
            .setAutoCancel(true)
            .setVisibility(NotificationCompat.VISIBILITY_PRIVATE)
            .build()
        manager.notify(id.hashCode(), notification)
        return Result.success()
    }
}
