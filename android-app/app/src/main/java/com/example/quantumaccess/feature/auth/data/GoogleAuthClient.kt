package com.example.quantumaccess.feature.auth.data

import android.content.Context
import android.content.Intent
import android.content.IntentSender
import com.example.quantumaccess.R
import com.google.android.gms.auth.api.identity.BeginSignInRequest
import com.google.android.gms.auth.api.identity.SignInClient
import java.util.concurrent.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
    private val oneTapClient: SignInClient
) {
    suspend fun signIn(): IntentSender? {
        val result = try {
            oneTapClient.beginSignIn(
                buildSignInRequest(autoSelect = true)
            ).await()
        } catch (e: Exception) {
            e.printStackTrace()
            if (e is CancellationException) throw e
            try {
                oneTapClient.beginSignIn(
                    buildSignInRequest(autoSelect = false)
                ).await()
            } catch (e2: Exception) {
                e2.printStackTrace()
                if (e2 is CancellationException) throw e2
                null
            }
        }
        return result?.pendingIntent?.intentSender
    }

    suspend fun signInWithIntent(intent: Intent): SignInResult {
        return try {
            val credential = oneTapClient.getSignInCredentialFromIntent(intent)
            val googleIdToken = credential.googleIdToken
            val username = credential.id
            val name = credential.displayName
            val profilePictureUrl = credential.profilePictureUri?.toString()
            val email = credential.id 

            if (googleIdToken != null) {
                 SignInResult(
                    data = UserData(
                        userId = username,
                        username = name ?: "",
                        profilePictureUrl = profilePictureUrl,
                        email = email,
                        idToken = googleIdToken // IMPORTANT: Added ID Token
                    ),
                    errorMessage = null
                )
            } else {
                SignInResult(
                    data = null,
                    errorMessage = "Google Sign In failed: No ID Token"
                )
            }
        } catch (e: Exception) {
             SignInResult(
                data = null,
                errorMessage = e.message
            )
        }
    }
    
    fun signOut() {
        oneTapClient.signOut()
    }

    private fun buildSignInRequest(autoSelect: Boolean): BeginSignInRequest {
        return BeginSignInRequest.builder()
            .setGoogleIdTokenRequestOptions(
                BeginSignInRequest.GoogleIdTokenRequestOptions.builder()
                    .setSupported(true)
                    // Replace with your actual Web Client ID from Google Cloud Console
                    .setServerClientId("889369247024-tgtf2ppnd9mik8vj39map1o755atdut8.apps.googleusercontent.com")
                    .setFilterByAuthorizedAccounts(false)
                    .build()
            )
            .setAutoSelectEnabled(autoSelect)
            .build()
    }
}

data class SignInResult(
    val data: UserData?,
    val errorMessage: String?
)

data class UserData(
    val userId: String,
    val username: String,
    val email: String,
    val profilePictureUrl: String?,
    val idToken: String? = null // Added field
)
