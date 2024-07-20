package net.horizonsend.ion.server.features.transport.step.origin.power

import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity

interface PowerOrigin {
	fun getTransferPower(destination: PoweredMultiblockEntity): Int

	val transferLimit: Int
}
