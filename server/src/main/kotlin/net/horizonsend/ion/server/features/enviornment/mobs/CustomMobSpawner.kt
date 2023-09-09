package net.horizonsend.ion.server.features.enviornment.mobs

import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ENTITY
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType.BOOLEAN

class CustomMobSpawner(val world: World, val mobs: WeightedRandomList<EntityType>) {
	// Expand this in the future with custom gear, etc

	fun handleSpawnEvent(event: CreatureSpawnEvent) {
		if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return
		if (event.entity !is Monster) return
		if (event.entity.persistentDataContainer.get(CUSTOM_ENTITY, BOOLEAN) == true) return

		event.isCancelled = true

		val location = event.location

		world.spawnEntity(location, mobs.random(), CreatureSpawnEvent.SpawnReason.NATURAL) {
			it.persistentDataContainer.set(CUSTOM_ENTITY, BOOLEAN, true)
			it.isPersistent = false
		}
	}
}
