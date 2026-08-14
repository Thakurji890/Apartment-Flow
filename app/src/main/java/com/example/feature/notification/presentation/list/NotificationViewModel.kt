package com.example.feature.notification.presentation.list

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.auth.domain.repository.AuthRepository
import com.example.feature.notification.domain.model.AppNotification
import com.example.feature.notification.domain.repository.NotificationRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import javax.inject.Inject

data class NotificationListState(
    val notifications: List<AppNotification> = emptyList(),
    val isLoading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class NotificationViewModel @Inject constructor(
    private val notificationRepository: NotificationRepository,
    private val authRepository: AuthRepository
) : ViewModel() {

    private val _state = MutableStateFlow(NotificationListState())
    val state = _state.asStateFlow()

    init {
        loadNotifications()
    }

    private fun loadNotifications() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            
            notificationRepository.getNotifications(user.uid)
                .onEach { result ->
                    when (result) {
                        is Resource.Success -> {
                            _state.value = _state.value.copy(
                                notifications = result.data ?: emptyList(),
                                isLoading = false,
                                error = null
                            )
                        }
                        is Resource.Error -> {
                            _state.value = _state.value.copy(
                                isLoading = false,
                                error = result.message
                            )
                        }
                        is Resource.Loading -> {}
                    }
                }
                .catch { e ->
                    _state.value = _state.value.copy(
                        isLoading = false,
                        error = e.message
                    )
                }
                .launchIn(this)
        }
    }

    fun markAsRead(notificationId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.markAsRead(user.uid, notificationId)
        }
    }

    fun markAllAsRead() {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.markAllAsRead(user.uid)
        }
    }

    fun deleteNotification(notificationId: String) {
        viewModelScope.launch {
            val user = authRepository.getCurrentUser() ?: return@launch
            notificationRepository.deleteNotification(user.uid, notificationId)
        }
    }
}
