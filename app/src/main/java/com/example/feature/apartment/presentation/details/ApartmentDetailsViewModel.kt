package com.example.feature.apartment.presentation.details

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.model.ApartmentMember
import com.example.feature.apartment.domain.model.InviteCode
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import com.example.feature.auth.domain.usecase.AuthUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ApartmentDetailsViewModel @Inject constructor(
    private val apartmentUseCases: ApartmentUseCases,
    private val authUseCases: AuthUseCases,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val apartmentId = savedStateHandle.get<String>("apartmentId") ?: ""

    private val _apartment = MutableStateFlow<Apartment?>(null)
    val apartment: StateFlow<Apartment?> = _apartment.asStateFlow()

    private val _members = MutableStateFlow<List<ApartmentMember>>(emptyList())
    val members: StateFlow<List<ApartmentMember>> = _members.asStateFlow()
    
    private val _inviteCode = MutableStateFlow<InviteCode?>(null)
    val inviteCode: StateFlow<InviteCode?> = _inviteCode.asStateFlow()

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _error = MutableStateFlow<String?>(null)
    val error: StateFlow<String?> = _error.asStateFlow()
    
    private val _currentUserId = MutableStateFlow<String?>(null)
    val currentUserId: StateFlow<String?> = _currentUserId.asStateFlow()

    init {
        loadData()
    }

    private fun loadData() {
        if (apartmentId.isBlank()) {
            _error.value = "Invalid apartment ID"
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            _isLoading.value = true
            
            _currentUserId.value = authUseCases.getCurrentUser()?.uid

            // Fetch Apartment
            when (val result = apartmentUseCases.getApartment(apartmentId)) {
                is Resource.Success -> {
                    _apartment.value = result.data
                    fetchMembers()
                    fetchInviteCode()
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                is Resource.Loading -> { }
            }
        }
    }

    private fun fetchMembers() {
        viewModelScope.launch {
            apartmentUseCases.getApartmentMembers(apartmentId).collect { result ->
                when (result) {
                    is Resource.Success -> {
                        _members.value = result.data ?: emptyList()
                        _isLoading.value = false
                    }
                    is Resource.Error -> {
                        _error.value = result.message
                        _isLoading.value = false
                    }
                    else -> {}
                }
            }
        }
    }
    
    private fun fetchInviteCode() {
        viewModelScope.launch {
            when (val result = apartmentUseCases.getInviteCode(apartmentId)) {
                is Resource.Success -> {
                    _inviteCode.value = result.data
                }
                else -> {}
            }
        }
    }
    
    fun generateNewInviteCode() {
        viewModelScope.launch {
            _isLoading.value = true
            when (val result = apartmentUseCases.generateInviteCode(apartmentId)) {
                is Resource.Success -> {
                    _inviteCode.value = result.data
                    _isLoading.value = false
                }
                is Resource.Error -> {
                    _error.value = result.message
                    _isLoading.value = false
                }
                else -> {}
            }
        }
    }
}
