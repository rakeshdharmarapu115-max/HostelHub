package com.hostelhub.app.data.remote.datasource

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.hostelhub.app.domain.model.User
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TokenManager @Inject constructor(
    @ApplicationContext private val context: Context,
    private val gson: Gson
) {
    private val prefs: SharedPreferences = context.getSharedPreferences("hostelhub_auth_prefs", Context.MODE_PRIVATE)

    private val _currentUserFlow = MutableStateFlow<User?>(getStoredUser())
    val currentUserFlow: StateFlow<User?> = _currentUserFlow.asStateFlow()

    private val _deallocationNoticeFlow = kotlinx.coroutines.flow.MutableSharedFlow<String>(replay = 1, extraBufferCapacity = 5)
    val deallocationNoticeFlow: kotlinx.coroutines.flow.SharedFlow<String> = _deallocationNoticeFlow

    fun notifyDeallocated(message: String = "Your hostel allocation has ended. You have been logged out.") {
        clearSession()
        _deallocationNoticeFlow.tryEmit(message)
    }

    fun getAccessToken(): String? {
        return prefs.getString(KEY_ACCESS_TOKEN, null)
    }

    fun getRefreshToken(): String? {
        return prefs.getString(KEY_REFRESH_TOKEN, null)
    }

    fun saveTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun saveAccessToken(accessToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .apply()
    }

    fun saveUser(user: User) {
        val userJson = gson.toJson(user)
        prefs.edit()
            .putString(KEY_USER, userJson)
            .apply()
        _currentUserFlow.value = user
    }

    fun getStoredUser(): User? {
        val userJson = prefs.getString(KEY_USER, null) ?: return null
        return try {
            gson.fromJson(userJson, User::class.java)
        } catch (e: Exception) {
            null
        }
    }

    fun clearSession() {
        prefs.edit()
            .remove(KEY_ACCESS_TOKEN)
            .remove(KEY_REFRESH_TOKEN)
            .remove(KEY_USER)
            .apply()
        _currentUserFlow.value = null
    }

    companion object {
        private const val KEY_ACCESS_TOKEN = "access_token"
        private const val KEY_REFRESH_TOKEN = "refresh_token"
        private const val KEY_USER = "user_json"
    }
}
