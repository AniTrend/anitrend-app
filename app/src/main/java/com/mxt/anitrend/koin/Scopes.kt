package com.mxt.anitrend.koin

import org.koin.core.qualifier.named

val ApplicationScopeQualifier = named("ApplicationScope")
val DefaultDispatcherQualifier = named("DefaultDispatcher")
val MainDispatcherQualifier = named("MainDispatcher")
val IoDispatcherQualifier = named("IoDispatcher")
val UnconfinedDispatcherQualifier = named("UnconfinedDispatcher")
