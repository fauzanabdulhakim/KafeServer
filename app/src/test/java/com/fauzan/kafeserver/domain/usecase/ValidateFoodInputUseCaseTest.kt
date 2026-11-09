package com.fauzan.kafeserver.domain.usecase

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit test for ValidateFoodInputUseCase validating merchant food inputs.
 */
class ValidateFoodInputUseCaseTest {

    private lateinit var useCase: ValidateFoodInputUseCase

    @Before
    fun setUp() {
        useCase = ValidateFoodInputUseCase()
    }

    @Test
    fun `validate with blank name should return error`() {
        val result = useCase(name = "   ", price = 15000L, description = "Tasty")
        assertFalse(result.isValid)
        assertEquals("Food name cannot be empty", result.errorMessage)
    }

    @Test
    fun `validate with negative price should return error`() {
        val result = useCase(name = "Espresso", price = -500L, description = "Hot")
        assertFalse(result.isValid)
        assertEquals("Food price cannot be negative", result.errorMessage)
    }

    @Test
    fun `validate with valid inputs should return success and trimmed data`() {
        val result = useCase(name = "  Caramel Macchiato  ", price = 28000L, description = "  Sweet and strong  ")
        assertTrue(result.isValid)
        assertEquals("Caramel Macchiato", result.trimmedName)
        assertEquals(28000L, result.sanitizedPrice)
        assertEquals("Sweet and strong", result.trimmedDescription)
    }
}
