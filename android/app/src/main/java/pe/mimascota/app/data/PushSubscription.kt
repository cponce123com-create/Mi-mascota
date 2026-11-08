package pe.mimascota.app.data

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import kotlinx.coroutines.tasks.await
import pe.mimascota.app.BuildConfig

/** The server never receives a push device token until the user explicitly opts in. */
object PushSubscription {
    private const val PREFS = "mi_mascota_push"

    fun isEnabled(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("enabled", false)

    fun hasPermission(context: Context) =
        Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) ==
            PackageManager.PERMISSION_GRANTED

    suspend fun enable(context: Context) {
        require(BuildConfig.API_BASE_URL.startsWith("https://")) { "Configura el servidor HTTPS" }
        require(hasPermission(context)) { "Autoriza las notificaciones primero" }
        val bearer = communityToken() ?: error("Inicia sesión con Google antes de activar avisos")
        val token = FirebaseMessaging.getInstance().token.await()
        CommunityApi(BuildConfig.API_BASE_URL).registerPushToken(token, bearer)
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean("enabled", true)
            .putBoolean("needs_reregister", false)
            .putString("registered_token", token)
            .apply()
    }

    /** Disable locally first, even if the remote server is temporarily offline. */
    suspend fun disable(context: Context): Boolean {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val token = prefs.getString("registered_token", null)
        prefs.edit().putBoolean("enabled", false).remove("registered_token").apply()
        val bearer = communityToken()
        val remoteRevoked = if (token != null && bearer != null &&
            BuildConfig.API_BASE_URL.startsWith("https://")) {
            runCatching {
                CommunityApi(BuildConfig.API_BASE_URL).unregisterPushToken(token, bearer)
            }.isSuccess
        } else token == null
        val localTokenDeleted = runCatching {
            FirebaseMessaging.getInstance().deleteToken().await()
        }.isSuccess
        return remoteRevoked && localTokenDeleted
    }
}
