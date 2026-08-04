package com.example.feature.auth.presentation.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.auth.domain.usecase.AuthUseCases
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

sealed class SplashDestination {
    object None : SplashDestination()
    object Onboarding : SplashDestination()
    object Welcome : SplashDestination()
    object ApartmentSetup : SplashDestination()
    object Dashboard : SplashDestination()
}

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val authUseCases: AuthUseCases,
    private val apartmentUseCases: ApartmentUseCases
) : ViewModel() {

    private val _destination = MutableStateFlow<SplashDestination>(SplashDestination.None)
    val destination: StateFlow<SplashDestination> = _destination.asStateFlow()

    init {
        checkDestination()
    }

    private fun checkDestination() {
        viewModelScope.launch {
            val user = authUseCases.getCurrentUser()
            val hasCompletedOnboarding = authUseCases.getOnboardingCompleted().first()

            if (user != null) {
                if (!user.isEmailVerified) {
                    _destination.value = SplashDestination.Welcome
                } else {
                    when (val result = apartmentUseCases.checkHasApartment()) {
                        is Resource.Success -> {
                            if (result.data) {
                                _destination.value = SplashDestination.Dashboard
                            } else {
                                _destination.value = SplashDestination.ApartmentSetup
                            }
                        }
                        else -> _destination.value = SplashDestination.ApartmentSetup
                    }
                }
            } else if (!hasCompletedOnboarding) {
                _destination.value = SplashDestination.Onboarding
            } else {
                _destination.value = SplashDestination.Welcome
            }
        }
    }
}
