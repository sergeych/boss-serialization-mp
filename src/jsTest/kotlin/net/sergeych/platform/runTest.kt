package net.sergeych.platform

import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.promise

actual fun runTest(block: suspend () -> Unit) { GlobalScope.promise { block() } }

