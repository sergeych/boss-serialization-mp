package net.sergeych.platform

import kotlinx.coroutines.DelicateCoroutinesApi
import net.sergeych.mp_tools.globalLaunch


@OptIn(DelicateCoroutinesApi::class)
actual fun runTest(block: suspend () -> Unit) { globalLaunch { block() } }
