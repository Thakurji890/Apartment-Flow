package com.example.feature.apartment.presentation.create

data class CreateApartmentState(
    val name: String = "",
    val nameError: String? = null,
    val address: String = "",
    val addressError: String? = null,
    val city: String = "",
    val state: String = "",
    val country: String = "",
    val pinCode: String = "",
    val currency: String = "USD",
    val timezone: String = "UTC",
    val monthlyRentDueDate: String = "1",
    val isLoading: Boolean = false
)

sealed class CreateApartmentEvent {
    data class NameChanged(val name: String) : CreateApartmentEvent()
    data class AddressChanged(val address: String) : CreateApartmentEvent()
    data class CityChanged(val city: String) : CreateApartmentEvent()
    data class StateChanged(val state: String) : CreateApartmentEvent()
    data class CountryChanged(val country: String) : CreateApartmentEvent()
    data class PinCodeChanged(val pinCode: String) : CreateApartmentEvent()
    data class CurrencyChanged(val currency: String) : CreateApartmentEvent()
    data class TimezoneChanged(val timezone: String) : CreateApartmentEvent()
    data class RentDueDateChanged(val dueDate: String) : CreateApartmentEvent()
    object Submit : CreateApartmentEvent()
}

sealed class CreateApartmentUiEvent {
    data class ShowSnackbar(val message: String) : CreateApartmentUiEvent()
    object Success : CreateApartmentUiEvent()
}
