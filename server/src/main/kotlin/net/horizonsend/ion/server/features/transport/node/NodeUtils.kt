package net.horizonsend.ion.server.features.transport.node

import net.horizonsend.ion.server.miscellaneous.utils.ADJACENT_BLOCK_FACES
import net.horizonsend.ion.server.miscellaneous.utils.associateWithNotNull
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import org.bukkit.block.BlockFace

fun getNeighborNodes(position: Long, nodes: Map<Long, TransportNode>, checkFaces: Collection<BlockFace> = ADJACENT_BLOCK_FACES) = checkFaces.associateWithNotNull {
	val x = getX(position)
	val y = getY(position)
	val z = getZ(position)

	nodes[toBlockKey(x + it.modX, y + it.modY, z + it.modZ)]
}

fun getNeighborNodes(position: Long, nodes: Collection<Long>, checkFaces: Collection<BlockFace> = ADJACENT_BLOCK_FACES) = checkFaces.mapNotNull {
	val x = getX(position)
	val y = getY(position)
	val z = getZ(position)

	toBlockKey(x + it.modX, y + it.modY, z + it.modZ).takeIf { key -> nodes.contains(key) }
}
