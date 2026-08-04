package com.example.feature.apartment.presentation.create

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.util.Resource
import com.example.feature.apartment.domain.model.Apartment
import com.example.feature.apartment.domain.usecase.ApartmentUseCases
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class CreateApartmentViewModel @Inject constructor(
    private val apartmentUseCases: ApartmentUseCases
) : ViewModel() {

    private val _state = MutableStateFlow(CreateApartmentState())
    val state: StateFlow<CreateApartmentState> = _state.asStateFlow()

    private val _eventFlow = MutableSharedFlow<CreateApartmentUiEvent>()
    val eventFlow: SharedFlow<CreateApartmentUiEvent> = _eventFlow.asSharedFlow()

    fun onEvent(event: CreateApartmentEvent) {
        when (event) {
            is CreateApartmentEvent.NameChanged -> _state.update { it.copy(name = event.name, nameError = null) }
            is CreateApartmentEvent.AddressChanged -> _state.update { it.copy(address = event.address, addressError = null) }
            is CreateApartmentEvent.CityChanged -> _state.update { it.copy(city = event.city) }
            is CreateApartmentEvent.StateChanged -> _state.update { it.copy(state = event.state) }
            is CreateApartmentEvent.CountryChanged -> _state.update { it.copy(country = event.country) }
            is CreateApartmentEvent.PinCodeChanged -> _state.update { it.copy(pinCode = event.pinCode) }
            is CreateApartmentEvent.CurrencyChanged -> _state.update { it.copy(currency = event.currency) }
            is CreateApartmentEvent.TimezoneChanged -> _state.update { it.copy(timezone = event.timezone) }
            is CreateApartmentEvent.RentDueDateChanged -> _state.update { it.copy(monthlyRentDueDate = event.dueDate) }
            is CreateApartmentEvent.Submit -> submitData()
        }
    }

    private fun submitData() {
        val nameResult = state.value.name.isNotBlank()
        val addressResult = state.value.address.isNotBlank()
        
        if (!nameResult || !addressResult) {
            _state.update {
                it.copy(
                    nameError = if (!nameResult) "Apartment name is required" else null,
                    addressError = if (!addressResult) "Address is required" else null
                )
            }
            return
        }

        val rentDue = state.value.monthlyRentDueDate.toIntOrNull() ?: 1

        viewModelScope.launch {
            _state.update { it.copy(isLoading = true) }
            
            val apartment = Apartment(
                name = state.value.name,
                address = state.value.address,
                city = state.value.city,
                state = state.value.state,
                country = state.value.country,
                pinCode = state.value.pinCode,
                currency = state.value.currency,
                timezone = state.value.timezone,
                monthlyRentDueDate = rentDue
            )
            
            when (val result = apartmentUseCases.createApartment(apartment)) {
                is Resource.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(CreateApartmentUiEvent.Success)
                }
                is Resource.Error -> {
                    _state.update { it.copy(isLoading = false) }
                    _eventFlow.emit(CreateApartmentUiEvent.ShowSnackbar(result.message))
                }
                is Resource.Loading -> {
                    _state.update { it.copy(isLoading = true) }
                }
            }
        }
    }
}
