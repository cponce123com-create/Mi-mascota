package pe.mimascota.app.auth

import android.app.Activity
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.tasks.await

class GoogleSignInManager(private val activity: Activity, private val webClientId: String) {
    suspend fun signIn(): Result<String> = runCatching {
        require(webClientId.isNotBlank()) { "Configura el ID de cliente web de Firebase" }
        val option = GetGoogleIdOption.Builder()
            .setServerClientId(webClientId)
            .setFilterByAuthorizedAccounts(false)
            .build()
        val request = GetCredentialRequest.Builder().addCredentialOption(option).build()
        val credential = CredentialManager.create(activity).getCredential(activity, request).credential
        val google = GoogleIdTokenCredential.createFrom(credential.data)
        val firebaseCredential = GoogleAuthProvider.getCredential(google.idToken, null)
        FirebaseAuth.getInstance().signInWithCredential(firebaseCredential).await().user?.uid
            ?: error("No se pudo autenticar al usuario")
    }
    fun signOut() { FirebaseAuth.getInstance().signOut() }
}
