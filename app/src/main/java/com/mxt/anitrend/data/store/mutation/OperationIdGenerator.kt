package com.mxt.anitrend.data.store.mutation

import java.util.UUID

fun interface OperationIdGenerator {
    fun generate(): String
}

class DefaultOperationIdGenerator : OperationIdGenerator {
    override fun generate(): String = UUID.randomUUID().toString()
}
