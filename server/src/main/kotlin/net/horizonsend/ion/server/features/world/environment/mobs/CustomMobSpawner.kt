package net.horizonsend.ion.server.features.world.environment.mobs

import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.WorldSettings
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ENTITY
import net.horizonsend.ion.server.miscellaneous.utils.weightedRandomOrNull
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType.BOOLEAN

class CustomMobSpawner(val world: IonWorld, mobs: List<WorldSettings.SpawnedMob>) {
	val mobs = mobs.plus(ConfigurationFiles.serverConfiguration().globalCustomSpawns)
	// Expand this in the future with custom gear, etc

	fun handleSpawnEvent(event: CreatureSpawnEvent) {
		if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return
		if (event.entity !is Monster) return
		if (event.entity.persistentDataContainer.get(CUSTOM_ENTITY, BOOLEAN) == true) return

		event.isCancelled = true

		val location = event.location

		val mob = mobs.weightedRandomOrNull { it.spawningWeight } ?: return

		if (!mob.function.get()) return

		val name = mob.namePool.entries.weightedRandomOrNull { it.value }

		world.world.spawnEntity(location, mob.getEntityType(), CreatureSpawnEvent.SpawnReason.NATURAL) { entity ->
			entity.persistentDataContainer.set(CUSTOM_ENTITY, BOOLEAN, true)
			name?.let { entity.customName(MiniMessage.miniMessage().deserialize(name.key)) }

			(entity as? LivingEntity)?.equipment?.apply {
				mob.boots?.let {
					this.boots = fromItemString(it.itemString)
					this.bootsDropChance = it.dropChance
				}
				mob.leggings?.let {
					this.leggings = fromItemString(it.itemString)
					this.leggingsDropChance = it.dropChance
				}
				mob.chestPlate?.let {
					this.chestplate = fromItemString(it.itemString)
					this.chestplateDropChance = it.dropChance
				}
				mob.helmet?.let {
					this.helmet = fromItemString(it.itemString)
					this.helmetDropChance = it.dropChance
				}
				mob.onHand?.let {
					this.setItemInMainHand(fromItemString(it.itemString))
					this.itemInMainHandDropChance = it.dropChance
				}
				mob.boots?.let {
					this.setItemInOffHand(fromItemString(it.itemString))
					this.itemInOffHandDropChance = it.dropChance
				}
			}

			entity.isPersistent = false
		}
	}
}
