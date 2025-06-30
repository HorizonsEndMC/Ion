package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface RegisteredInput {
	val holder: MultiblockEntity

	class Simple(override val holder: MultiblockEntity) : RegisteredInput

	interface RegisteredMetaDataInput<T: InputMetaData> : RegisteredInput {
		val metaData: T
	}

	sealed interface InputMetaData
}
