package com.fauzan.kafeserver.domain.usecase

/**
 * Domain UseCase validating food input before merchant updates or creations.
 * Follows Clean Architecture with zero Android framework dependencies.
 */
class ValidateFoodInputUseCase {

    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String? = null,
        val trimmedName: String = "",
        val sanitizedPrice: Long = 0L,
        val trimmedDescription: String = ""
    )

    /**
     * Validates merchant food attributes.
     *
     * @param name input name
     * @param price input price
     * @param description input description
     * @return ValidationResult indicating validation status and sanitized values
     */
    operator fun invoke(name: String?, price: Long, description: String?): ValidationResult {
        val trimmedName = name?.trim().orEmpty()
        val trimmedDesc = description?.trim().orEmpty()

        if (trimmedName.isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Food name cannot be empty"
            )
        }

        if (price < 0) {
            return ValidationResult(
                isValid = false,
                errorMessage = "Food price cannot be negative"
            )
        }

        return ValidationResult(
            isValid = true,
            trimmedName = trimmedName,
            sanitizedPrice = price,
            trimmedDescription = trimmedDesc
        )
    }
}
