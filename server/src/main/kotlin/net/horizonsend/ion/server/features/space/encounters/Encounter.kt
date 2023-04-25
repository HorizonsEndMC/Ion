package net.horizonsend.ion.server.features.space.encounters

import net.minecraft.nbt.CompoundTag
import org.bukkit.World
import org.bukkit.event.player.PlayerInteractEvent

/**
 * A basic class controlling an encounter on a wreck.
 *
 * @property constructChestNBT Code used when generating the primary chest on the wreck.
 * 	This is executed when it places the chest block.
 * @property onChestInteract Code executed when the primary chest is interacted with.
 * @property generate Additional instructions executed when generating the wreck.
 **/
abstract class Encounter(val identifier: String) {
	abstract fun constructChestNBT(): CompoundTag

	open fun onChestInteract(event: PlayerInteractEvent) {}

	open fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}

	companion object {
		val baseChestNBT = CompoundTag().apply { this.putString("id", "minecraft:chest") }
	}
}
