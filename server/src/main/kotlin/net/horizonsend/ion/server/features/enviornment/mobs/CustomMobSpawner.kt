package net.horizonsend.ion.server.features.enviornment.mobs

import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys.CUSTOM_ENTITY
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.World
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Monster
import org.bukkit.event.entity.CreatureSpawnEvent
import org.bukkit.persistence.PersistentDataType.BOOLEAN

class CustomMobSpawner(val world: World, val mobs: WeightedRandomList<ServerConfiguration.PlanetSpawnConfig.Mob>) {
	// Expand this in the future with custom gear, etc

	fun handleSpawnEvent(event: CreatureSpawnEvent) {
		if (event.spawnReason != CreatureSpawnEvent.SpawnReason.NATURAL) return
		if (event.entity !is Monster) return
		if (event.entity.persistentDataContainer.get(CUSTOM_ENTITY, BOOLEAN) == true) return

		event

		event.isCancelled = true

		val location = event.location

		val mob = mobs.random()
		val name = mob.nameList.randomOrNull()

		world.spawnEntity(location, mob.getEntityType(), CreatureSpawnEvent.SpawnReason.NATURAL) { entity ->
			entity.persistentDataContainer.set(CUSTOM_ENTITY, BOOLEAN, true)
			name?.let { entity.customName(MiniMessage.miniMessage().deserialize(name)) }

			(entity as? LivingEntity)?.equipment?.apply {
				mob.boots?.let {
					this.boots = Bazaars.fromItemString(it.itemString)
					this.bootsDropChance = it.dropChance
				}
				mob.leggings?.let {
					this.leggings = Bazaars.fromItemString(it.itemString)
					this.leggingsDropChance = it.dropChance
				}
				mob.chestPlate?.let {
					this.chestplate = Bazaars.fromItemString(it.itemString)
					this.chestplateDropChance = it.dropChance
				}
				mob.helmet?.let {
					this.helmet = Bazaars.fromItemString(it.itemString)
					this.helmetDropChance = it.dropChance
				}
				mob.onHand?.let {
					this.setItemInMainHand(Bazaars.fromItemString(it.itemString))
					this.itemInMainHandDropChance = it.dropChance
				}
				mob.boots?.let {
					this.setItemInOffHand(Bazaars.fromItemString(it.itemString))
					this.itemInOffHandDropChance = it.dropChance
				}
			}

			entity.isPersistent = false
		}
	}
}
