package net.horizonsend.ion.server.features.transport.node.power

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity

/**
 *
 **/
interface PowerNode {
	val multiblocks: MutableList<PoweredMultiblockEntity>
}
