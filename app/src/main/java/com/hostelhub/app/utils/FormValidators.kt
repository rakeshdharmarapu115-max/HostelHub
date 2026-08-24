package com.hostelhub.app.utils

object FormValidators {
    fun validateEmail(email: String): ValidationResult {
        if (email.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Email is required")
        }
        val emailPattern = "^[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$".toRegex()
        if (!emailPattern.matches(email.trim())) {
            return ValidationResult(isValid = false, errorMessage = "Please enter a valid email address")
        }
        return ValidationResult(isValid = true)
    }

    fun validatePassword(password: String): ValidationResult {
        if (password.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Password is required")
        }
        if (password.length < 8) {
            return ValidationResult(isValid = false, errorMessage = "Password must be at least 8 characters")
        }
        val hasLetter = password.any { it.isLetter() }
        val hasDigit = password.any { it.isDigit() }
        if (!hasLetter || !hasDigit) {
            return ValidationResult(isValid = false, errorMessage = "Password must include at least one letter and one number")
        }
        return ValidationResult(isValid = true)
    }

    fun validateFullName(name: String): ValidationResult {
        if (name.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Full name is required")
        }
        if (name.trim().length < 3) {
            return ValidationResult(isValid = false, errorMessage = "Name must be at least 3 characters")
        }
        return ValidationResult(isValid = true)
    }

    fun validateStudentId(studentId: String): ValidationResult {
        if (studentId.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Student ID is required")
        }
        if (studentId.trim().length < 3) {
            return ValidationResult(isValid = false, errorMessage = "Please enter a valid student ID")
        }
        return ValidationResult(isValid = true)
    }

    fun validatePhone(phone: String): ValidationResult {
        if (phone.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "Phone number is required")
        }
        val cleaned = phone.replace("[^0-9+]".toRegex(), "")
        if (cleaned.length < 7 || cleaned.length > 15) {
            return ValidationResult(isValid = false, errorMessage = "Please enter a valid phone number")
        }
        return ValidationResult(isValid = true)
    }

    fun validateRequired(value: String, fieldName: String): ValidationResult {
        if (value.isBlank()) {
            return ValidationResult(isValid = false, errorMessage = "$fieldName is required")
        }
        return ValidationResult(isValid = true)
    }
}

data class ValidationResult(
    val isValid: Boolean,
    val errorMessage: String? = null
)
