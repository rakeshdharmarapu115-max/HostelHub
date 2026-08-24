package com.hostelhub.app.utils

sealed interface UiState<out T> {
    data object Idle : UiState<Nothing>
    data object Loading : UiState<Nothing>
    data class Success<T>(val data: T) : UiState<T>
    data class Empty(val message: String = "No data found") : UiState<Nothing>
    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}
