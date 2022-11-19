package net.horizonsend.ion.server.networks.nodes

import net.horizonsend.ion.server.networks.connections.AbstractConnection
import net.horizonsend.ion.server.networks.connections.DirectConnection
import net.minecraft.world.level.block.Blocks
import kotlin.math.min

class ExtractorNode : AbstractNode() {
	// Extractors do not allow power to transfer through them
	override fun canStepFrom(lastNode: AbstractNode, lastConnection: AbstractConnection): Boolean = false

	fun tick() {
		if (!isLoaded) return

		var computerConnection: AbstractConnection? = null
		var computer: ComputerNode? = null

		for (connection in connections) {
			if (connection !is DirectConnection) continue
			val foundComputer = connection.other(this) as? ComputerNode ?: continue
			if (!foundComputer.isValid) continue
			computerConnection = connection
			computer = foundComputer
			break
		}

		if (computerConnection == null || computer == null) return

		var availablePower = min(2000, computer.powerValue)
		if (availablePower <= 0) return

		typedPathfind<ComputerNode> {
			val consumedPower = it.consume(availablePower)
			availablePower -= consumedPower
			it.powerValue -= consumedPower

			availablePower <= 0
		}
	}

	companion object : AbstractNodeCompanion<ExtractorNode>(Blocks.CRAFTING_TABLE) {
		override fun construct(): ExtractorNode = ExtractorNode()
	}
}