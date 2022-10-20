package net.starlegacy.util

import co.aikar.timings.Timing
import co.aikar.timings.Timings
import net.horizonsend.ion.server.IonServer.Companion.plugin

fun timing(name: String): Timing = Timings.of(plugin, name)

fun <T> Timing.time(block: () -> T): T = apply { startTiming() }.use { block() }