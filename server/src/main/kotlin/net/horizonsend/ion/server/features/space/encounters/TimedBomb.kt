package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.runnable
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.spherePoints
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.event.player.PlayerInteractEvent

object TimedBomb : Encounter(identifier = "timed_bomb") {
	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!
		val chest = (targetedBlock.state as? Chest) ?: return
		Encounters.setChestFlag(chest, NamespacedKeys.INACTIVE, "true")

		val timeLimit = 30 // seconds
		var iteration = 0 // ticks
		event.player.alert("Timed bomb activated! Loot the wreck and get out in $timeLimit seconds before the explosion!")
		runnable {
			if (iteration % 5 == 0) {
				targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
			}
			if (iteration >= timeLimit * 20 - 100) {
				targetedBlock.location.world.playSound(targetedBlock.location, Sound.BLOCK_NOTE_BLOCK_BELL, 5.0f, 1.0f)
			}
			if (timeLimit * 20 == iteration) {
				val explosionRadius = 10.0 // For spawning actual explosions
				val explosionDamage = 100.0
				val explosionDamageRadius = 20.0 // For entity damage calculation
				targetedBlock.location.spherePoints(explosionRadius, 10).forEach {
					it.createExplosion(10.0f)
				}
				targetedBlock.location.getNearbyLivingEntities(explosionDamageRadius).forEach {
					it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(targetedBlock.location)) / explosionDamageRadius)
				}
				cancel()
			}
			iteration++
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/gun_parts")
	}
}
