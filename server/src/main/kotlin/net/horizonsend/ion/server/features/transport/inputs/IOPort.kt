package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface IOPort {
	val holder: MultiblockEntity

	class Simple(override val holder: MultiblockEntity) : IOPort

	class RegisteredMetaDataInput<T: InputMetaData>(override val holder: MultiblockEntity, val metaData: T) : IOPort {}

	interface InputMetaData
}
