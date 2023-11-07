package net.horizonsend.ion.server.features.multiblock.farming

import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.PowerStoringMultiblock
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getBlockIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.Block
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

abstract class CropMultiblock : Multiblock(), PowerStoringMultiblock, FurnaceMultiblock {
	abstract val regionRadius: Int
	abstract val regionDepth: Int
	abstract val regionHeight: Int

	/** Get the origin of harvest point. Usually right behind the multiblock */
	protected abstract fun getOriginOffset(): Vec3i

	/** Get the iterable of the blocks contained in the crop region */
	protected fun regionIterable(sign: Sign): Iterable<Block> {
		val blocks = mutableSetOf<Block>()

		val signOrigin = Vec3i(sign.location)
		val regionOrigin = getRegionOriginPosition(sign) + signOrigin

		val facing = sign.getFacing().oppositeFace

		val originBlock = getBlockIfLoaded(sign.world, regionOrigin.x, regionOrigin.y, regionOrigin.z) ?: return  blocks
		blocks += originBlock

		for (depth in 0..regionDepth) {
			val depthOffset = originBlock.getRelativeIfLoaded(facing, depth) ?: continue

			for (width in -regionRadius..+regionRadius) {
				val widthOffset = depthOffset.getRelativeIfLoaded(facing.rightFace, width) ?: continue

				for (height in 0..regionHeight) {
					val heightOffset = widthOffset.getRelativeIfLoaded(BlockFace.UP, height) ?: continue

					blocks += heightOffset
				}
			}
		}

		return blocks
	}

	private fun getRegionOriginPosition(sign: Sign): Vec3i {
		val (x, y, z) = getOriginOffset()
		val facing = sign.getFacing()
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = y,
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}
}
