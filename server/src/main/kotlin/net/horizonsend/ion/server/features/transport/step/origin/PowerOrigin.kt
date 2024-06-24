package net.horizonsend.ion.server.features.transport.step.origin

import net.horizonsend.ion.server.features.multiblock.entity.type.PoweredMultiblockEntity

interface PowerOrigin {
	fun getTransferPower(destination: PoweredMultiblockEntity): Int
}
