package net.horizonsend.ion.server.features.space.encounters

import net.horizonsend.ion.common.extensions.alert
import net.minecraft.nbt.CompoundTag
import org.bukkit.World
import org.bukkit.entity.EntityType
import org.bukkit.event.player.PlayerInteractEvent

object ItsATrap : Encounter(identifier = "its_a_trap") {
	override fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}

	override fun onChestInteract(event: PlayerInteractEvent) {
		val targetedBlock = event.clickedBlock!!
		event.player.alert("it worked")
		for (count in 0..100) {
			targetedBlock.location.world.spawnEntity(targetedBlock.location, EntityType.LIGHTNING)
		}
	}

	override fun constructChestNBT(): CompoundTag {
		return Encounters.createLootChest("horizonsend:chests/guns")
	}
}
