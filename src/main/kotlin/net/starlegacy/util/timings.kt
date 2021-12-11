package net.starlegacy.util

import co.aikar.timings.Timing
import co.aikar.timings.Timings
import net.starlegacy.PLUGIN

fun timing(name: String): Timing = Timings.of(PLUGIN, name)

fun <T> Timing.time(block: () -> T): T = apply { startTiming() }.use { block() }
