package com.hostelhub.app.domain.model

data class Admin(
    val adminId: String = "",
    val userId: String = "",
    val fullName: String = "",
    val associationName: String = "",
    val designation: String = "",
    val permissions: List<String> = listOf("ALL"),
    val contactPhone: String = ""
)
