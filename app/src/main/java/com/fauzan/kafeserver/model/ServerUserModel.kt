package com.fauzan.kafeserver.model

/**
 * Server staff/admin user entity.
 */
data class ServerUserModel(
    var uid: String? = null,
    var name: String? = null,
    var phone: String? = null,
    var isActive: Boolean = false
)
