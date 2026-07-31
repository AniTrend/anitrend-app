package com.mxt.anitrend.data.store.mutation

import kotlin.coroutines.cancellation.CancellationException

class SessionInvalidatedException : CancellationException("Session invalidated by logout")
