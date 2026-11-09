package com.fauzan.kafeserver.domain.session

import com.fauzan.kafeserver.model.CategoryModel
import com.fauzan.kafeserver.model.FoodModel
import com.fauzan.kafeserver.model.ServerUserModel

/**
 * Thread-safe scoped session manager for the staff/admin application.
 * Encapsulates mutable session data, eliminating static field anti-patterns.
 */
class ServerSessionManager {

    var currentServerUser: ServerUserModel? = null
    var selectedCategory: CategoryModel? = null
    var selectedFood: FoodModel? = null

    val isAuthenticated: Boolean
        get() = currentServerUser != null

    @Synchronized
    fun clearSession() {
        currentServerUser = null
        selectedCategory = null
        selectedFood = null
    }

    companion object {
        @Volatile
        private var instance: ServerSessionManager? = null

        @JvmStatic
        fun getInstance(): ServerSessionManager {
            return instance ?: synchronized(this) {
                instance ?: ServerSessionManager().also { instance = it }
            }
        }

        @JvmStatic
        fun resetInstance() {
            synchronized(this) {
                instance = null
            }
        }
    }
}
