package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.runnable
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.minecraft.nbt.CompoundTag
import net.starlegacy.util.spherePoints
import org.bukkit.Sound
import org.bukkit.block.Chest
import org.bukkit.entity.EntityType
import org.bukkit.entity.LivingEntity
import org.bukkit.event.player.PlayerInteractEvent

object CowTipper : Encounter(identifier = "cow_tipper") {
	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return

		if (Encounters.getChestFlag(chest, LOCKED) == "true") {
			event.player.userError("You must slaughter the Explosive Cow before opening the chest!")
			return
		}

		Encounters.setChestFlag(chest, LOCKED, "true")

		val explosiveCow = targetedBlock.location.world.spawnEntity(targetedBlock.location, EntityType.COW)
		explosiveCow.customName(Component.text("Explosive Cow", NamedTextColor.RED))
		val timeLimit = 15 // seconds
		var iteration = 0 // ticks
		event.player.alert("Slaughter the Explosive Cow in $timeLimit seconds or perish!!!")

		runnable {
			if (iteration % 5 == 0) {
				explosiveCow.location.world.playSound(explosiveCow, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 5.0f, 1.0f)
			}
			if (iteration >= timeLimit * 20 - 100) {
				explosiveCow.location.world.playSound(explosiveCow, Sound.BLOCK_NOTE_BLOCK_COW_BELL, 5.0f, 1.0f)
			}
			if (explosiveCow.isDead) {
				Encounters.setChestFlag(chest, LOCKED, "false")
				Encounters.setChestFlag(chest, NamespacedKeys.INACTIVE, "true")
				event.player.success("The Explosive Cow was put down! The chest is now unlocked.")
				cancel()
			}
			if (timeLimit * 20 == iteration) {
				val explosionRadius = 7.5 // For spawning actual explosions
				val explosionDamage = 100.0
				val explosionDamageRadius = 15.0 // For entity damage calculation
				explosiveCow.location.spherePoints(explosionRadius, 10).forEach {
					it.createExplosion(7.5f)
				}
				explosiveCow.location.getNearbyLivingEntities(explosionRadius).forEach {
					it.damage(explosionDamage * (explosionDamageRadius - it.location.distance(explosiveCow.location)) / explosionDamageRadius, explosiveCow)
				}
				if (!explosiveCow.isDead) {
					(explosiveCow as LivingEntity).damage(explosionDamage)
				}
				Encounters.setChestFlag(chest, LOCKED, "false")
				event.player.userError("You were tipped by the Explosive Cow!")
				cancel()
			}
			iteration++
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("minecraft:chests/abandoned_mineshaft")
	}
}
