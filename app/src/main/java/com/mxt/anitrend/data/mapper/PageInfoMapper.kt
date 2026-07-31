package com.mxt.anitrend.data.mapper

import com.mxt.anitrend.domain.model.PageInfoRecord
import com.mxt.anitrend.model.entity.container.attribute.PageInfo

fun PageInfo.toPageInfoRecord(): PageInfoRecord {
    val totalValue = total.takeIf { it > 0 }
    val perPageValue = perPage.takeIf { it > 0 }
    val currentPageValue = currentPage.takeIf { it > 0 }
    val lastPageValue =
        if (totalValue != null && perPageValue != null && perPageValue > 0) {
            ((totalValue + perPageValue - 1) / perPageValue)
        } else {
            null
        }

    return PageInfoRecord(
        currentPage = currentPageValue,
        lastPage = lastPageValue,
        perPage = perPageValue,
        total = totalValue,
        hasNextPage = hasNextPage(),
        hasPreviousPage = (currentPageValue ?: 0) > 1,
    )
}
