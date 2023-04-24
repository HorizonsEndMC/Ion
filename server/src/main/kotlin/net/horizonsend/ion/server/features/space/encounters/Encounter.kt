package net.horizonsend.ion.server.features.space.encounters

import net.minecraft.nbt.CompoundTag
import net.minecraft.world.level.block.state.BlockState
import net.starlegacy.util.nms
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * A basic class controlling an encounter on a wreck.
 *
 * @property constructChestState Code used when generating the primary chest on the wreck.
 * 	This is executed when it places the chest block.
 * @property onChestInteract Code executed when the primary chest is interacted with.
 * @property generate Additional instructions executed when generating the wreck.
 **/
abstract class Encounter(val identifier: String) {
	open fun constructChestState(): Pair<BlockState, CompoundTag?> {
		return Material.CHEST.createBlockData().nms to null
	}

	open fun onChestInteract(event: PlayerInteractEvent) {}

	open fun generate(world: World, chestX: Int, chestY: Int, chestZ: Int) {}
}
