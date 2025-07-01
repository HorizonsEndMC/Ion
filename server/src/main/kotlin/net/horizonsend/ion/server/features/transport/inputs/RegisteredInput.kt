package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface RegisteredInput {
	val holder: MultiblockEntity

	class Simple(override val holder: MultiblockEntity) : RegisteredInput

	class RegisteredMetaDataInput<T: InputMetaData>(override val holder: MultiblockEntity, val metaData: T) : RegisteredInput {}

	interface InputMetaData
}
