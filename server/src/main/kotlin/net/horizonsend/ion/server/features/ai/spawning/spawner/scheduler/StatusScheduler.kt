package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.kyori.adventure.text.Component

interface StatusScheduler {
	fun getStatus(): Component
}
