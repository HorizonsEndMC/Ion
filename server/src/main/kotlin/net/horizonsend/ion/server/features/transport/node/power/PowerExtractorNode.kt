package net.horizonsend.ion.server.features.transport.node.power

import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.node.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.GridNode
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

class PowerExtractorNode(
	parentGrid: Grid,
	x: Int,
	y: Int,
	z: Int,
	transferableNeighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap(),
	val solarPanel: Boolean = isSolarPanel(parentGrid, x, y, z)
) : ExtractorNode<NamespacedKey>(parentGrid, x, y, z, transferableNeighbors) {
	var lastTicked: Long = System.currentTimeMillis()

	override fun isTransferableTo(offset: BlockFace, node: GridNode): Boolean {
		// Don't send power to other extractors
		if (node is PowerExtractorNode) return false

		// Power may only exit from an input
		if (node is PowerInputNode) return false

		// All others allowed
		return true
	}

	fun tickSolarPanel(): Int {
		val time = System.currentTimeMillis()
		val deltaMs = time - lastTicked
		lastTicked = time

		val deltaSeconds = deltaMs / 1000.0
		return getPower(deltaSeconds)
	}

	private fun getPower(delta: Double): Int {
		val environment = parentGrid.world.environment

		val power = 5.0 / if (environment == World.Environment.NORMAL) 1.0 else 2.0

		return (power * delta).toInt()
	}

	companion object {
		fun isSolarPanel(parentGrid: Grid, x: Int, y: Int, z: Int): Boolean = runBlocking {
			val aboveOne = getBlockSnapshotAsync(parentGrid.world, x, y + 1, z, false)
			val aboveTwo = getBlockSnapshotAsync(parentGrid.world, x, y + 2, z, false)

			return@runBlocking aboveOne?.type == Material.DIAMOND_BLOCK && aboveTwo?.type == Material.DAYLIGHT_DETECTOR
		}
	}
}
