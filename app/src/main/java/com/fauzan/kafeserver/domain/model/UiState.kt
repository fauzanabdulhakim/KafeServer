package com.fauzan.kafeserver.domain.model

/**
 * Unified reactive UI State representation for merchant and kitchen operations.
 *
 * @param T data payload type
 */
sealed interface UiState<out T> {

    object Idle : UiState<Nothing>

    object Loading : UiState<Nothing>

    object Empty : UiState<Nothing>

    data class Success<T>(val data: T) : UiState<T>

    data class Error(val message: String, val cause: Throwable? = null) : UiState<Nothing>
}
