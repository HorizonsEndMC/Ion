package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.common.database.cache.AIEncounterCache
import net.horizonsend.ion.common.database.schema.misc.AIEncounterData

interface PersistentScheduler {
	fun loadData()
	fun saveData()
}
