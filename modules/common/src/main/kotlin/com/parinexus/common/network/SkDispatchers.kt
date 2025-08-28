package com.parinexus.common.network

import javax.inject.Qualifier
import kotlin.annotation.AnnotationRetention.RUNTIME

@Qualifier
@Retention(RUNTIME)
annotation class Dispatcher(val niaDispatcher: SkDispatchers)

enum class SkDispatchers {
    Default,
    IO,
}
