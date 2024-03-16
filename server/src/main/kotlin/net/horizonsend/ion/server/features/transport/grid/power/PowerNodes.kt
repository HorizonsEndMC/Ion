package net.horizonsend.ion.server.features.transport.grid.power

import net.horizonsend.ion.server.features.transport.grid.GridNode
import org.bukkit.block.BlockFace
import java.util.concurrent.ConcurrentHashMap

abstract class PowerNode(
	override val x: Int,
	override val y: Int,
	override val z: Int,
	override val neighbors: ConcurrentHashMap<BlockFace, GridNode> = ConcurrentHashMap()
) : GridNode {

}
