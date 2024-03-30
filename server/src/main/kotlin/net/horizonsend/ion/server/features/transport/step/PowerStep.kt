package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.container.NamespacedResourceContainer
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.transport.node.GridNode
import org.bukkit.block.BlockFace

class PowerStep(
	override val grid: PowerGrid,
	override val origin: NamespacedResourceContainer?,
	direction: BlockFace,
	currentNode: GridNode
) : Step(grid, origin, direction, currentNode)
