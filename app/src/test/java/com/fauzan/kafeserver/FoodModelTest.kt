package com.fauzan.kafeserver

import com.fauzan.kafeserver.model.FoodModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit tests verifying FoodModel properties including Out-of-Stock (86-ing) availability status.
 */
class FoodModelTest {

    @Test
    fun foodModel_defaultAvailability_shouldBeTrue() {
        val food = FoodModel(name = "Cappuccino", price = 25000L)
        assertTrue(food.isAvailable)
    }

    @Test
    fun foodModel_whenMarkedOutOfStock_shouldBeFalse() {
        val food = FoodModel(name = "Croissant", price = 18000L, isAvailable = false)
        assertFalse(food.isAvailable)
    }

    @Test
    fun foodModel_toggleAvailability_shouldUpdateState() {
        val food = FoodModel(name = "Matcha Latte", price = 30000L)
        assertTrue(food.isAvailable)

        food.isAvailable = false
        assertFalse(food.isAvailable)

        food.isAvailable = true
        assertTrue(food.isAvailable)
    }
}
