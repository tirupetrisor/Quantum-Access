package com.example.quantumaccess.data.repository

import com.example.quantumaccess.data.local.SecurePrefsManager
import com.example.quantumaccess.data.local.dao.SessionDao
import com.example.quantumaccess.data.local.dao.TransactionDao
import com.example.quantumaccess.data.local.dao.UserDao
import com.example.quantumaccess.data.local.entities.LocalUserEntity
import com.example.quantumaccess.data.local.entities.SessionEntity
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.providers.builtin.IDToken
import io.github.jan.supabase.postgrest.from
import io.github.jan.supabase.exceptions.RestException
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.put
import kotlin.text.RegexOption
import java.time.Instant
import android.util.Base64
import java.util.UUID
import javax.inject.Inject

import android.util.Log

class AuthRepository @Inject constructor(
    private val prefs: SecurePrefsManager,
    private val userDao: UserDao,
    private val transactionDao: TransactionDao,
    private val sessionDao: SessionDao,
    private val supabase: SupabaseClient
) {

    suspend fun logout(): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val biometricEnabled = prefs.isBiometricEnabled()

                if (!biometricEnabled) {
                    supabase.auth.signOut()
                    transactionDao.deleteAll()
                    sessionDao.clearSession()
                } else {
                    // When biometric is enabled we keep the remote session and cached data so that
                    // the user can re-authenticate offline and we still have a refresh token inside Supabase.
                    // We still refresh the heartbeat so the local session does not expire immediately.
                    refreshSessionHeartbeat()
                }
                Result.success(Unit)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun register(
        name: String,
        username: String,
        email: String,
        password: String,
        deviceId: String,
        biometricEnabled: Boolean
    ): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                // 1. Sign Up with Supabase Auth
                val signUpError = runCatching {
                    supabase.auth.signUpWith(Email) {
                        this.email = email
                        this.password = password
                    }
                }.exceptionOrNull()

                if (signUpError != null) {
                    return@withContext Result.failure(mapSignUpError(signUpError))
                }

                val user = supabase.auth.currentUserOrNull()
                
                if (user == null) {
                     return@withContext Result.failure(
                         RegistrationException(
                             RegistrationException.Type.CONFIRMATION_REQUIRED,
                             "Account created. Please check your email to confirm."
                         )
                     )
                }

                val userId = UUID.fromString(user.id)

                // 2. Insert into Remote 'users' table (via PostgREST)
                val userDto = buildJsonObject {
                    put("user_id", userId.toString())
                    put("username", username)
                    put("email", email)
                    put("name", name)
                    put("biometric_enabled", biometricEnabled)
                }

                val insertError = runCatching {
                    supabase.from("users").insert(userDto)
                }.exceptionOrNull()

                if (insertError != null) {
                    val conflictResolved = runCatching {
                        handleProfileInsertConflict(insertError, userId, userDto)
                    }.getOrDefault(false)

                    if (!conflictResolved) {
                        return@withContext Result.failure(mapProfileInsertError(insertError))
                    }
                }

                // 3. Cache Locally
                val localUser = LocalUserEntity(
                    userId = userId,
                    username = username,
                    email = email,
                    name = name,
                    biometricEnabled = biometricEnabled,
                    googleId = null
                )
                userDao.insert(localUser)
                
                prefs.setBiometricEnabled(biometricEnabled)
                
                Result.success(Unit)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun login(email: String, password: String): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                supabase.auth.signInWith(Email) {
                    this.email = email
                    this.password = password
                }
                
                syncUserProfile().getOrThrow()
                persistLocalSession()
                Result.success(Unit)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun googleSignIn(
        idToken: String?, 
        rawNonce: String? = null,
        biometricEnabled: Boolean = false
    ): Result<Unit> {
        return try {
             withContext(Dispatchers.IO) {
                 if (idToken != null) {
                     val extractedGoogleId = extractGoogleId(idToken)
                     // Use IDToken provider for native Android tokens
                     supabase.auth.signInWith(IDToken) {
                         this.idToken = idToken
                         this.provider = Google
                         this.nonce = rawNonce
                     }
                     syncUserProfile(biometricEnabled, extractedGoogleId).getOrThrow()
                     persistLocalSession()
                     Result.success(Unit)
                 } else {
                     Result.failure(IllegalArgumentException("ID Token required for Google Sign-In"))
                 }
             }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    suspend fun getCurrentUser(): LocalUserEntity? {
        val authUser = supabase.auth.currentUserOrNull() ?: return null
        return userDao.getById(UUID.fromString(authUser.id))
    }

    /**
     * Gets the locally cached user without requiring an active Supabase session.
     * Useful for biometric login when Supabase session may have expired.
     */
    suspend fun getLocalUser(): LocalUserEntity? {
        return withContext(Dispatchers.IO) {
            userDao.getCurrentUser().first()
        }
    }

    suspend fun updateBiometricStatus(enabled: Boolean): Result<Unit> {
        return try {
            withContext(Dispatchers.IO) {
                val authUser = supabase.auth.currentUserOrNull()
                    ?: return@withContext Result.failure(IllegalStateException("User not logged in"))
                
                val userId = UUID.fromString(authUser.id)
                
                // Update Remote
                supabase.from("users").update(
                    buildJsonObject { put("biometric_enabled", enabled) }
                ) {
                    filter { eq("user_id", userId.toString()) }
                }
                
                // Update Local
                val localUser = userDao.getById(userId)
                if (localUser != null) {
                    userDao.insert(localUser.copy(biometricEnabled = enabled))
                }
                
                prefs.setBiometricEnabled(enabled)
                
                Result.success(Unit)
            }
        } catch (t: Throwable) {
            Result.failure(t)
        }
    }

    private suspend fun syncUserProfile(
        biometricEnabled: Boolean = false,
        providedGoogleId: String? = null
    ): Result<Unit> {
        val authUser = supabase.auth.currentUserOrNull() ?: return Result.failure(IllegalStateException("User not logged in"))
        val userId = UUID.fromString(authUser.id)
        
        // Fetch from Remote DB
        return try {
            val remoteUserResponse = supabase.from("users").select {
                filter { eq("user_id", userId.toString()) }
            }.decodeList<JsonObject>()
            
            if (remoteUserResponse.isEmpty()) {
                // If user doesn't exist in 'users' table (e.g. first time Google Sign-In), insert them
                // Improved username generation to avoid conflicts: timestamp suffix
                val baseUsername = authUser.userMetadata?.get("name")?.toString()?.replace(" ", "")?.filter { it.isLetterOrDigit() } ?: "User"
                val timestampSuffix = System.currentTimeMillis().toString().takeLast(6)
                val finalUsername = "$baseUsername$timestampSuffix"

                val newUser = buildJsonObject {
                    put("user_id", userId.toString())
                    put("username", finalUsername)
                    put("email", authUser.email ?: "")
                    put("name", authUser.userMetadata?.get("full_name")?.toString() ?: authUser.email ?: "")
                    put("biometric_enabled", biometricEnabled)
                    if (!providedGoogleId.isNullOrBlank()) {
                        put("google_id", providedGoogleId)
                    }
                }
                
                // Use upsert to ignore conflicts if race conditions occur, or standard insert if policies allow
                supabase.from("users").insert(newUser)
                
                // Recursively call to cache locally after insertion
                return syncUserProfile(biometricEnabled)
            }

            val remoteUser = remoteUserResponse.first()
            val username = remoteUser.getString("username") ?: "Unknown"
            val email = remoteUser.getString("email")
            val name = remoteUser.getString("name") ?: "Unknown"
            val biometricFlag = remoteUser.getBoolean("biometric_enabled")
            val remoteGoogleId = remoteUser.getString("google_id")
            val resolvedGoogleId = remoteGoogleId ?: providedGoogleId

            if (remoteGoogleId.isNullOrBlank() && !providedGoogleId.isNullOrBlank()) {
                supabase.from("users").update(
                    buildJsonObject { put("google_id", providedGoogleId) }
                ) {
                    filter { eq("user_id", userId.toString()) }
                }
            }
            
            // Update Local
            val localUser = LocalUserEntity(
                userId = userId,
                username = username,
                email = email,
                name = name,
                biometricEnabled = biometricFlag,
                googleId = resolvedGoogleId
            )
            userDao.insert(localUser) // Insert or Replace
            
            prefs.setBiometricEnabled(localUser.biometricEnabled)
            Result.success(Unit)
            
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    private suspend fun persistLocalSession() {
        val session = supabase.auth.currentSessionOrNull() ?: return
        val user = session.user ?: return
        val userId = UUID.fromString(user.id)
        val token = session.accessToken ?: session.refreshToken ?: UUID.randomUUID().toString()
        val now = Instant.now()
        val expiresAt = now.plusSeconds(SESSION_TIMEOUT_MINUTES * 60)
        val entity = SessionEntity(
            token = token,
            userId = userId,
            lastActiveAt = now,
            expiresAt = expiresAt
        )
        sessionDao.insertSession(entity)
    }

    suspend fun refreshSessionHeartbeat() {
        val session = sessionDao.getLatestSession() ?: return
        val now = Instant.now()
        val refreshed = session.copy(
            lastActiveAt = now,
            expiresAt = now.plusSeconds(SESSION_TIMEOUT_MINUTES * 60)
        )
        sessionDao.insertSession(refreshed)
    }

    suspend fun isSessionExpired(): Boolean {
        val session = sessionDao.getLatestSession() ?: return true
        return session.expiresAt.isBefore(Instant.now())
    }

    /**
     * Attempts to refresh the Supabase session if it has expired.
     * Returns true if session is active (either was already active or was successfully refreshed).
     */
    suspend fun refreshSupabaseSessionIfNeeded(): Boolean {
        return withContext(Dispatchers.IO) {
            try {
                val currentSession = supabase.auth.currentSessionOrNull()
                if (currentSession != null) {
                    // Session exists and is valid
                    persistLocalSession()
                    return@withContext true
                }
                
                // Try to refresh using stored refresh token
                try {
                    supabase.auth.refreshCurrentSession()
                    val refreshedSession = supabase.auth.currentSessionOrNull()
                    
                    if (refreshedSession != null) {
                        // Successfully refreshed - update local session
                        persistLocalSession()
                        return@withContext true
                    }
                } catch (e: Exception) {
                    // Refresh failed - session expired or no refresh token
                    Log.w(TAG, "Session refresh failed (harmless for offline mode)", e)
                }
                
                return@withContext false
            } catch (e: Exception) {
                Log.e(TAG, "Unexpected error during session refresh check", e)
                return@withContext false
            }
        }
    }

    private fun extractGoogleId(idToken: String): String? {
        return runCatching {
            val segments = idToken.split(".")
            if (segments.size < 2) return null
            val payload = segments[1]
            val decoded = Base64.decode(payload, Base64.URL_SAFE or Base64.NO_WRAP or Base64.NO_PADDING)
            val payloadJson = String(decoded, Charsets.UTF_8)
            val key = "\"sub\":\""
            val startIndex = payloadJson.indexOf(key)
            if (startIndex == -1) return null
            val endIndex = payloadJson.indexOf("\"", startIndex + key.length)
            if (endIndex == -1) return null
            payloadJson.substring(startIndex + key.length, endIndex)
        }.getOrNull()
    }

    companion object {
        private const val TAG = "AuthRepository"
        private const val SESSION_TIMEOUT_MINUTES = 15L
    }

    private fun JsonObject.getString(key: String): String? {
        return (this[key] as? JsonPrimitive)?.contentOrNull
    }

    private fun JsonObject.getBoolean(key: String): Boolean {
        val value = this[key] ?: return false
        return when (value) {
            is JsonPrimitive -> {
                value.booleanOrNull
                    ?: value.intOrNull?.let { it != 0 }
                    ?: value.contentOrNull?.equals("true", ignoreCase = true)
                    ?: false
            }
            else -> false
        }
    }

    private fun mapSignUpError(error: Throwable): RegistrationException {
        val rawMessage = error.message ?: "Registration failed"
        val lowerMessage = rawMessage.lowercase()

        return when {
            lowerMessage.contains("for security") && lowerMessage.contains("second") -> {
                val seconds = extractSecondsFromMessage(rawMessage)
                val friendly = if (seconds != null) {
                    "Too many attempts. Please wait $seconds seconds before trying again."
                } else {
                    "Too many attempts. Please try again in a moment."
                }
                RegistrationException(RegistrationException.Type.RATE_LIMITED, friendly, error)
            }
            lowerMessage.contains("already registered") || lowerMessage.contains("email") && lowerMessage.contains("registered") -> {
                RegistrationException(RegistrationException.Type.EMAIL_TAKEN, "Email already in use.", error)
            }
            isNetworkIssue(lowerMessage) -> {
                RegistrationException(
                    RegistrationException.Type.NETWORK,
                    "Network error. Check your connection and try again.",
                    error
                )
            }
            else -> RegistrationException(RegistrationException.Type.UNKNOWN, rawMessage, error)
        }
    }

    private fun mapProfileInsertError(error: Throwable): RegistrationException {
        val pgError = error as? RestException
        val detailMessage = pgError?.description?.takeIf { it.isNotBlank() }
        val rawMessage = detailMessage ?: pgError?.message ?: (error.message ?: "Failed to store user profile.")
        val normalized = rawMessage.lowercase()

        return when {
            normalized.contains("users_username_key") || normalized.contains("(username)=") -> {
                RegistrationException(RegistrationException.Type.USERNAME_TAKEN, "Username already taken.", error)
            }
            normalized.contains("(email)=") || normalized.contains("users_email_key") -> {
                RegistrationException(RegistrationException.Type.EMAIL_TAKEN, "Email already in use.", error)
            }
            isNetworkIssue(normalized) -> {
                RegistrationException(
                    RegistrationException.Type.NETWORK,
                    "Network error. Check your connection and try again.",
                    error
                )
            }
            else -> RegistrationException(
                RegistrationException.Type.UNKNOWN,
                rawMessage,
                error
            )
        }
    }

    private suspend fun handleProfileInsertConflict(
        error: Throwable,
        userId: UUID,
        userDto: JsonObject
    ): Boolean {
        val pgError = error as? RestException ?: return false
        val detail = pgError.description?.lowercase() ?: pgError.message?.lowercase() ?: return false
        val isSameUserConflict = detail.contains("(user_id)=") ||
            detail.contains("users_pkey") ||
            detail.contains("users_user_id_key")

        if (!isSameUserConflict) return false

        val updatePayload = buildJsonObject {
            put("username", userDto.getValue("username"))
            put("email", userDto.getValue("email"))
            put("name", userDto.getValue("name"))
            put("biometric_enabled", userDto.getValue("biometric_enabled"))
        }

        return runCatching {
            supabase.from("users").update(updatePayload) {
                filter { eq("user_id", userId.toString()) }
            }
            true
        }.getOrDefault(false)
    }

    private fun extractSecondsFromMessage(message: String): Int? {
        val regex = "(\\d+)\\s*(?:second|seconds)".toRegex(RegexOption.IGNORE_CASE)
        return regex.find(message)?.groupValues?.getOrNull(1)?.toIntOrNull()
    }

    private fun isNetworkIssue(text: String): Boolean {
        return text.contains("network") ||
            text.contains("timeout") ||
            text.contains("unable to resolve host") ||
            text.contains("failed to connect") ||
            text.contains("connection refused")
    }
}
