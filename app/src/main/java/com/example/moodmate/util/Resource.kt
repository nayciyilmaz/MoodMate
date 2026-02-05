package com.example.moodmate.util

sealed class Resource<T>(
    val data: T? = null,
    val message: String? = null,
    val fieldErrors: Map<String, String>? = null,
    val isUnauthorized: Boolean = false
) {
    class Success<T>(data: T) : Resource<T>(data)
    class Error<T>(
        message: String,
        data: T? = null,
        fieldErrors: Map<String, String>? = null,
        isUnauthorized: Boolean = false
    ) : Resource<T>(data, message, fieldErrors, isUnauthorized)
    class Loading<T> : Resource<T>()
}