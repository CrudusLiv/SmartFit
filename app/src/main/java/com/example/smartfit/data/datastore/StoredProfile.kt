package com.example.smartfit.data.datastore

data class StoredProfile(
    val id: String,
    val displayName: String,
    val email: String? = null
)
