package net.horizonsend.ion.server.networks.connections

import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.properties.DirectionProperty

class WiredConnection : AbstractConnection() {
	override fun checkIsValid(): Boolean {
		if (!super.checkIsValid()) return false

		val sameX = a.x == b.x
		val sameY = a.y == b.y

		val direction = if (!sameY) Direction.UP else if (sameX) Direction.SOUTH else Direction.EAST
		val oppositeDirection = direction.opposite

		val xIter = if (a.x > b.x) b.x .. a.x else a.x .. b.x
		val yIter = if (a.y > b.y) b.y .. a.y else a.y .. b.y
		val zIter = if (a.z > b.z) b.z .. a.z else a.z .. b.z

		for (x in xIter) for (y in yIter) for (z in zIter) {
			if (x == a.x && y == a.y && z == a.z) continue
			if (x == b.x && y == b.y && z == b.z) continue

			val blockState = a.ionChunk.ionWorld.serverLevel.getBlockStateIfLoaded(BlockPos(x, y, z)) ?: continue
			if (blockState.block != Blocks.END_ROD) return false

			val blockDirection = blockState.properties.find { it is DirectionProperty }?.getValue("facing")?.orElse(null) as Direction?

			if (blockDirection != direction && blockDirection != oppositeDirection) return false
		}

		return true
	}

	companion object : AbstractConnectionCompanion<WiredConnection>() {
		override fun construct(): WiredConnection = WiredConnection()
	}
}