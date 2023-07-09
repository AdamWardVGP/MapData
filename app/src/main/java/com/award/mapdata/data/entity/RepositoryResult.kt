package com.award.mapdata.data.entity

sealed class RepositoryResult<T> {
    class Success<T>(val payload: T) : RepositoryResult<T>()
    class Failure<T>(
        val Exception: Exception? = null,
        val message: String? = null
    ) : RepositoryResult<T>()
}