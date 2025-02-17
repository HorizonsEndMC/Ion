package net.horizonsend.ion.server.features.multiblock.entity.type

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.block.Furnace
import org.bukkit.inventory.FurnaceInventory
import java.time.Duration

/**
 * Common interface for multiblocks with a furnace at their origin. Provides utils for dealing with them.
 **/
interface FurnaceBasedMultiblockEntity {
	/**
	 * Gets the furnace block entity. Must be called sync
	 **/
	fun getFurnace(): Furnace? {
		Tasks.checkMainThread()

		this as MultiblockEntity
		return getInventory(0, 0, 0)?.holder as? Furnace
	}
	/**
	 * Gets the furnace block entity. Must be called sync
	 **/
	fun getFurnaceInventory(): FurnaceInventory? {
		Tasks.checkMainThread()

		this as MultiblockEntity
		return getInventory(0, 0, 0) as? FurnaceInventory
	}

	/** Sets the furnace to burn, by executing a sync task */
	fun setBurningFor(duration: Duration) {
		Tasks.checkMainThread()

		val furnace = getFurnace() ?: return
		furnace.burnTime = (duration.toMillis() / 50).toShort()
		furnace.update()
	}

	fun setBurningForTicks(ticks: Int) {
		Tasks.checkMainThread()

		val furnace = getFurnace() ?: return
		furnace.burnTime = ticks.toShort()
		furnace.update()
	}

	fun putOutFurnace() {
		Tasks.checkMainThread()

		val furnace = getFurnace() ?: return
		furnace.burnTime = 0.toShort()
		furnace.update()
	}
}
