package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.INACTIVE
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.LOCKED
import net.horizonsend.ion.server.miscellaneous.runnable
import net.minecraft.nbt.CompoundTag
import org.bukkit.block.BlockFace
import org.bukkit.block.Chest
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent

object Infested : Encounter(identifier = "infested") {
	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!

		event.isCancelled = true
		val chest = (targetedBlock.state as? Chest) ?: return

		if (Encounters.getChestFlag(chest, LOCKED) == "true") {
			event.player.userError("You must dispatch the Endermites before opening the chest!")
			return
		}

		Encounters.setChestFlag(chest, LOCKED, "true")

		val endermites = mutableListOf<Entity>()
		for (i in 0..4) {
			val endermite = targetedBlock.location.world.spawnEntity(targetedBlock.getRelative(BlockFace.UP).location,
				EntityType.ENDERMITE
			)
			endermites.add(endermite)
		}

		val timeLimit = 20 // seconds
		var iteration = 0 // ticks
		event.player.alert("Seems like this chest is infested with Endermites! Eliminate the pests in $timeLimit seconds!")

		runnable {
			if (endermites.all { it.isDead } ) {
				Encounters.setChestFlag(chest, LOCKED, "false")
				Encounters.setChestFlag(chest, INACTIVE, "true")
				event.player.success("The infestation was removed and the chest is now unlocked.")
				cancel()
			}
			if (timeLimit * 20 == iteration || event.player.isDead) {
				endermites.forEach { it.remove() }
				Encounters.setChestFlag(chest, LOCKED, "false")
				event.player.userError("You could not remove the infestation! The Endermites have returned to the chest.")
				cancel()
			}
			iteration++
		}.runTaskTimer(IonServer, 0L, 1L)
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/guns")
	}
}
