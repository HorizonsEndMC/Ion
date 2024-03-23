package net.horizonsend.ion.server.features.transport.grid.power.node

import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.grid.node.GridNode
import org.bukkit.World
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

class PowerExtractorNode(
	override val parentGrid: Grid,
	override val x: Int,
	override val y: Int,
	override val z: Int,
	override val neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap(),
	val solarPanel: Boolean = false
) : GridNode {
	var lastTicked: Long = System.currentTimeMillis()

	fun tickSolarPanel() {
		val time = System.currentTimeMillis()
		val deltaMs = time - lastTicked
		lastTicked = time

		val deltaSeconds = deltaMs / 1000.0
		val power = getPower(deltaSeconds)
	}

	private fun getPower(delta: Double): Int {
		val environment = parentGrid.world.environment

		val power = 5.0 / if (environment == World.Environment.NORMAL) 1.0 else 2.0

		return (power * delta).toInt()
	}

	companion object {
		fun isSolarPanel(parentGrid: Grid, x: Int, y: Int, z: Int): Boolean = TODO()
	}
}
