package net.horizonsend.ion.server.features.custom.blocks.misc

import net.horizonsend.ion.server.features.transport.util.getBlockEntity
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.minecraft.world.level.block.entity.CommandBlockEntity
import org.bukkit.World

interface GaugeCustomBlock {
	/**
	 * Sets the redstone output of this gauge, returns whether the signal could be set.
	 **/
	fun setSignalOutput(value: Int, world: World, blockLocation: Vec3i): Boolean

	object CommandBlockGaugeCustomBlock : GaugeCustomBlock {
		override fun setSignalOutput(value: Int, world: World, blockLocation: Vec3i): Boolean {
			val entity = getBlockEntity(blockLocation, world) as? CommandBlockEntity ?: return false
			val oldSuccessCount = entity.commandBlock.successCount

			if (value != oldSuccessCount) {
				entity.commandBlock.successCount = value
				Tasks.sync {
					entity.level?.updateNeighbourForOutputSignal(entity.blockPos, entity.blockState.block)
				}

				return true
			}

			return false
		}
	}
}
