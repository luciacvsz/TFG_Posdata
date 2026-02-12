package com.posdata.app.ui.screens.preferences

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.posdata.app.data.local.UserInfo
import com.posdata.app.data.remote.RetrofitClient
import com.posdata.app.data.repository.UserRepository
import com.posdata.app.model.*
import kotlinx.coroutines.launch

class PreferencesViewModel(private val repository: UserRepository) : ViewModel() {

    fun updateColorScheme(newColor: AppColorScheme) {
        viewModelScope.launch {
            repository.updateSettings(color = newColor)
        }
    }

    fun updateFontSize(isLarge: Boolean) {
        viewModelScope.launch {
            val size = if (isLarge) AppFontSize.LARGE else AppFontSize.REGULAR
            repository.updateSettings(fontSize = size)
        }
    }

    fun updateNotificationSound(isEnabled: Boolean) {
        viewModelScope.launch {
            val sound = if (isEnabled) AppNotificationSound.ON else AppNotificationSound.OFF
            repository.updateSettings(sound = sound)
        }
    }

    fun updateExplanationMode(isEnabled: Boolean) {
        viewModelScope.launch {
            val mode = if (isEnabled) AppExplanationMode.ON else AppExplanationMode.OFF
            repository.updateSettings(explanation = mode)
        }
    }

    fun updateExhaustivity(isEnabled: Boolean) {
        viewModelScope.launch {
            val mode = if (isEnabled) AppExhaustivity.ENHANCED else AppExhaustivity.REGULAR
            repository.updateSettings(exhaustivity = mode)
        }
    }
}

class SettingsViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PreferencesViewModel::class.java)) {

            val userInfo = UserInfo(context)

            val localAPi = RetrofitClient.localInstance
            val cloudApi = RetrofitClient.cloudInstance

            val repository = UserRepository(localAPi, cloudApi, userInfo)

            @Suppress("UNCHECKED_CAST")
            return PreferencesViewModel(repository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}