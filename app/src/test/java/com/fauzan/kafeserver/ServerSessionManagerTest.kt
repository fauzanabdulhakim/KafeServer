package com.fauzan.kafeserver

import com.fauzan.kafeserver.domain.session.ServerSessionManager
import com.fauzan.kafeserver.model.CategoryModel
import com.fauzan.kafeserver.model.FoodModel
import com.fauzan.kafeserver.model.ServerUserModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Unit tests verifying ServerSessionManager state lifecycle and encapsulation.
 */
class ServerSessionManagerTest {

    private lateinit var sessionManager: ServerSessionManager

    @Before
    fun setUp() {
        sessionManager = ServerSessionManager()
    }

    @Test
    fun initialState_shouldBeUnauthenticated() {
        assertFalse(sessionManager.isAuthenticated)
        assertNull(sessionManager.currentServerUser)
        assertNull(sessionManager.selectedCategory)
        assertNull(sessionManager.selectedFood)
    }

    @Test
    fun setCurrentServerUser_shouldAuthenticateUser() {
        val user = ServerUserModel(
            name = "Manager Fauzan",
            phone = "+628123456789"
        )

        sessionManager.currentServerUser = user

        assertTrue(sessionManager.isAuthenticated)
        assertNotNull(sessionManager.currentServerUser)
        assertEquals("Manager Fauzan", sessionManager.currentServerUser?.name)
        assertEquals("+628123456789", sessionManager.currentServerUser?.phone)
    }

    @Test
    fun categoryAndFoodSelection_shouldBePersistedInSession() {
        val category = CategoryModel(name = "Espresso Beverages")
        val food = FoodModel(name = "Caramel Macchiato")

        sessionManager.selectedCategory = category
        sessionManager.selectedFood = food

        assertEquals("Espresso Beverages", sessionManager.selectedCategory?.name)
        assertEquals("Caramel Macchiato", sessionManager.selectedFood?.name)
    }

    @Test
    fun clearSession_shouldResetAllStateToNull() {
        val user = ServerUserModel(name = "Staff")
        sessionManager.currentServerUser = user

        val category = CategoryModel()
        sessionManager.selectedCategory = category

        sessionManager.clearSession()

        assertFalse(sessionManager.isAuthenticated)
        assertNull(sessionManager.currentServerUser)
        assertNull(sessionManager.selectedCategory)
        assertNull(sessionManager.selectedFood)
    }
}
