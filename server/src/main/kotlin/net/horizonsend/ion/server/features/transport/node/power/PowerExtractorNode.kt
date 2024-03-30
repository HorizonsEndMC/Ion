package net.horizonsend.ion.server.features.transport.node.power

import kotlinx.coroutines.runBlocking
import net.horizonsend.ion.server.features.multiblock.util.getBlockSnapshotAsync
import net.horizonsend.ion.server.features.transport.container.ResourceContainer
import net.horizonsend.ion.server.features.transport.grid.Grid
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.transport.node.ExtractorNode
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.features.transport.step.PowerStep
import net.horizonsend.ion.server.features.transport.step.Step
import org.bukkit.Material
import org.bukkit.NamespacedKey
import org.bukkit.World
import org.bukkit.block.BlockFace

class PowerExtractorNode(
	override val parentGrid: PowerGrid,
	x: Int,
	y: Int,
	z: Int,
	val solarPanel: Boolean = isSolarPanel(parentGrid, x, y, z)
) : ExtractorNode<NamespacedKey>(parentGrid, x, y, z) {
	var lastTicked: Long = System.currentTimeMillis()

	override fun getExtractableInventories(): Collection<ResourceContainer<NamespacedKey>> {
		TODO("Not yet implemented")
	}

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

		val deltaSeconds = deltaMs / 1000.0
		return getPower(deltaSeconds)
	}

	private fun getPower(delta: Double): Int {
		val environment = parentGrid.world.environment

		val power = 5.0 / if (environment == World.Environment.NORMAL) 1.0 else 2.0

		return (power * delta).toInt()
	}

	override fun processStep(step: Step) {
		println("Stepping extractor")
	}

	fun startStep(): Step {
		val direction = transferableNeighbors.keys().toList().random()

		return PowerStep(
			grid = parentGrid,
			origin = null,
			direction = direction,
			this
		)
	}

	companion object {
		fun isSolarPanel(parentGrid: Grid, x: Int, y: Int, z: Int): Boolean = runBlocking {
			val aboveOne = getBlockSnapshotAsync(parentGrid.world, x, y + 1, z, false)
			val aboveTwo = getBlockSnapshotAsync(parentGrid.world, x, y + 2, z, false)

			return@runBlocking aboveOne?.type == Material.DIAMOND_BLOCK && aboveTwo?.type == Material.DAYLIGHT_DETECTOR
		}
	}
}
