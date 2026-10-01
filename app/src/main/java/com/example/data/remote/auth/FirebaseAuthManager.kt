package com.example.data.remote.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import androidx.credentials.exceptions.GetCredentialException
import com.example.data.local.logging.AppLogger
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.firebase.FirebaseApp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

data class AuthUser(
    val uid: String,
    val email: String?,
    val displayName: String?,
    val photoUrl: String?
)

sealed interface AuthState {
    data object Unauthenticated : AuthState
    data object Loading : AuthState
    data class Authenticated(val user: AuthUser) : AuthState
    data class Error(val message: String) : AuthState
    data class ConfigurationRequired(val message: String) : AuthState
}

/**
 * Production Firebase Authentication & Google Sign-In Manager.
 *
 * Implements Android Credential Manager flow with Google ID Option.
 * Handles missing google-services.json, user cancellations, and network disconnects gracefully.
 */
class FirebaseAuthManager(private val context: Context) {

    private val _authState = MutableStateFlow<AuthState>(AuthState.Unauthenticated)
    val authState: StateFlow<AuthState> = _authState.asStateFlow()

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    val currentUser: StateFlow<AuthUser?> = _currentUser.asStateFlow()

    private var firebaseAuth: FirebaseAuth? = null

    init {
        checkInitialStatus()
    }

    private fun checkInitialStatus() {
        try {
            if (FirebaseApp.getApps(context).isNotEmpty()) {
                val auth = FirebaseAuth.getInstance()
                firebaseAuth = auth
                val fbUser = auth.currentUser
                if (fbUser != null) {
                    val user = AuthUser(
                        uid = fbUser.uid,
                        email = fbUser.email,
                        displayName = fbUser.displayName,
                        photoUrl = fbUser.photoUrl?.toString()
                    )
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                } else {
                    _authState.value = AuthState.Unauthenticated
                }
            } else {
                _authState.value = AuthState.ConfigurationRequired(
                    "Google login configuration incomplete hai (Firebase google-services.json required)."
                )
            }
        } catch (e: Exception) {
            AppLogger.warn("Auth", "InitCheck", "Firebase Auth not ready", e)
            _authState.value = AuthState.ConfigurationRequired(
                "Google login configuration incomplete hai: ${e.message}"
            )
        }
    }

    suspend fun signInWithGoogle(webClientId: String? = null): Result<AuthUser> = withContext(Dispatchers.IO) {
        _authState.value = AuthState.Loading

        if (FirebaseApp.getApps(context).isEmpty()) {
            val msg = "Google login configuration incomplete hai (Firebase google-services.json required)."
            _authState.value = AuthState.ConfigurationRequired(msg)
            AppLogger.warn("Auth", "GoogleSignIn", msg)
            return@withContext Result.failure(Exception(msg))
        }

        val auth = firebaseAuth ?: try {
            FirebaseAuth.getInstance().also { firebaseAuth = it }
        } catch (e: Exception) {
            val msg = "Firebase Auth initialization failed: ${e.message}"
            _authState.value = AuthState.Error(msg)
            return@withContext Result.failure(Exception(msg))
        }

        val effectiveClientId = webClientId?.takeIf { it.isNotBlank() }
            ?: "1452848496689087.apps.googleusercontent.com" // Default placeholder if not injected

        try {
            AppLogger.info("Auth", "GoogleSignIn", "Launching Google Sign-In via Credential Manager")
            val credentialManager = CredentialManager.create(context)

            val googleIdOption = GetGoogleIdOption.Builder()
                .setFilterByAuthorizedAccounts(false)
                .setServerClientId(effectiveClientId)
                .setAutoSelectEnabled(false)
                .build()

            val request = GetCredentialRequest.Builder()
                .addCredentialOption(googleIdOption)
                .build()

            val response = credentialManager.getCredential(context, request)
            val credential = response.credential

            if (credential is androidx.credentials.CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL
            ) {
                val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                val idToken = googleIdTokenCredential.idToken

                val firebaseCredential = GoogleAuthProvider.getCredential(idToken, null)
                val authResult = auth.signInWithCredential(firebaseCredential).await()
                val fbUser = authResult.user

                if (fbUser != null) {
                    val user = AuthUser(
                        uid = fbUser.uid,
                        email = fbUser.email,
                        displayName = fbUser.displayName,
                        photoUrl = fbUser.photoUrl?.toString()
                    )
                    _currentUser.value = user
                    _authState.value = AuthState.Authenticated(user)
                    AppLogger.info("Auth", "GoogleSignIn", "User authenticated successfully: ${user.email ?: user.uid}")
                    return@withContext Result.success(user)
                } else {
                    val err = "Authentication succeeded but no user profile returned."
                    _authState.value = AuthState.Error(err)
                    return@withContext Result.failure(Exception(err))
                }
            } else {
                val err = "Unexpected credential format returned."
                _authState.value = AuthState.Error(err)
                return@withContext Result.failure(Exception(err))
            }
        } catch (e: GetCredentialCancellationException) {
            AppLogger.info("Auth", "GoogleSignIn", "User cancelled Google Sign-In")
            _authState.value = AuthState.Unauthenticated
            return@withContext Result.failure(Exception("Sign in cancelled."))
        } catch (e: GetCredentialException) {
            val friendlyMsg = when {
                e.message?.contains("16", ignoreCase = true) == true || e.message?.contains("Cannot find a matching credential", ignoreCase = true) == true ->
                    "Google login configuration incomplete hai. Please ensure Web Client ID and SHA-1 are registered in Firebase Console."
                e.message?.contains("network", ignoreCase = true) == true ->
                    "Internet connection nahi hai. Please check your network connection."
                else ->
                    "Google Sign-In error: ${e.message ?: "Unknown error"}"
            }
            AppLogger.warn("Auth", "GoogleSignIn", friendlyMsg, e)
            _authState.value = AuthState.Error(friendlyMsg)
            return@withContext Result.failure(Exception(friendlyMsg))
        } catch (e: Exception) {
            val friendlyMsg = "Authentication error: ${e.localizedMessage ?: "Please try again."}"
            AppLogger.error("Auth", "GoogleSignIn", friendlyMsg, e)
            _authState.value = AuthState.Error(friendlyMsg)
            return@withContext Result.failure(Exception(friendlyMsg))
        }
    }

    fun signOut() {
        try {
            firebaseAuth?.signOut()
            _currentUser.value = null
            _authState.value = AuthState.Unauthenticated
            AppLogger.info("Auth", "SignOut", "User signed out")
        } catch (e: Exception) {
            AppLogger.warn("Auth", "SignOut", "Sign out error", e)
        }
    }
}
