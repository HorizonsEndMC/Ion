package net.horizonsend.ion.server.miscellaneous.utils

import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.data.BlockData
import java.util.concurrent.ConcurrentHashMap

object LegacyBlockUtils {
	val PIPE_DIRECTIONS = arrayOf(
		BlockFace.DOWN,
		BlockFace.UP,
		BlockFace.NORTH,
		BlockFace.SOUTH,
		BlockFace.WEST,
		BlockFace.EAST,
		BlockFace.UP
	)

	fun isInside(location: Location, extraChecks: Int): Boolean {
		val originalBlock = getBlockIfLoaded(location.world, location.blockX, location.blockY, location.blockZ)
			?: return false

		val airBlocks = ConcurrentHashMap.newKeySet<Block>()

		val directions = PIPE_DIRECTIONS

		quickLoop@ for (direction in directions) {
			if (direction.oppositeFace == direction) {
				continue
			}

			var block: Block?

			for (i in 0..254) {
				block = originalBlock.getRelativeIfLoaded(direction, i) ?: return false

				if (getBlockTypeSafe(block.world, block.x, block.y, block.z)?.isAir == false) {
					if (i != 0) {
						val airBlock = originalBlock.getRelativeIfLoaded(direction, i - 1)
						airBlocks?.add(airBlock)
					}
					continue@quickLoop
				}
			}

			return false
		}

		var check = 0

		while (check <= extraChecks && !airBlocks.isEmpty()) {
			edgeLoop@ for (airBlock in airBlocks)
				for (direction in directions) {
					if (direction.oppositeFace == direction) continue
					var block: Block?
					for (i in 0..254) {
						block = airBlock.getRelativeIfLoaded(direction, i)
						if (block == null) return false

						if (getBlockTypeSafe(block.world, block.x, block.y, block.z)?.isAir == false) {
							if (i != 0) {
								val newAirBlock = airBlock.getRelativeIfLoaded(direction, i)
								if (newAirBlock != null) {
									airBlocks.add(newAirBlock)
								}
							}
							airBlocks.remove(airBlock)
							continue@edgeLoop
						}
					}
					return false
				}
			check++
		}

		return true
	}

	fun getRotatedBlockData(data: BlockData, rotation: Rotation): BlockState {
		val nms = data.nms
		val customBlock = IonRegistries.CUSTOM_BLOCKS[nms]
		if (customBlock != null) {
			return CustomBlockRegistry.getRotated(customBlock, nms, rotation)
		}

		return nms.rotate(rotation)
	}
}
