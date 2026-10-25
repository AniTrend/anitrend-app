package com.mxt.anitrend.ui.review

import com.mxt.anitrend.domain.model.ReviewRecord

internal fun distinctReviewRows(reviews: List<ReviewRecord>): List<ReviewRecord> = reviews.distinctBy { review -> review.id }
