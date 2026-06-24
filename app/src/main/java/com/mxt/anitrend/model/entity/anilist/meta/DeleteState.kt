package com.mxt.anitrend.model.entity.anilist.meta

import com.google.gson.annotations.SerializedName

/**
 * Mutation for deleted items
 */
class DeleteState(
    @SerializedName("deleted")
    val isDeleted: Boolean,
)
