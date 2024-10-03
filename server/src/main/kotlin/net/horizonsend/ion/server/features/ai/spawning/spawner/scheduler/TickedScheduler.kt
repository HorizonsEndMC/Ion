package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import org.slf4j.Logger

interface TickedScheduler {
	fun tick(logger: Logger)
}
