package com.mxt.anitrend.domain.model

data class PageInfoRecord(
    val currentPage: Int?,
    val lastPage: Int?,
    val perPage: Int?,
    val total: Int?,
    val hasNextPage: Boolean,
    val hasPreviousPage: Boolean,
)
