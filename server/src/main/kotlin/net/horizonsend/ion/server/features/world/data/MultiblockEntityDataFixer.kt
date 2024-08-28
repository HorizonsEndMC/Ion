package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import kotlin.reflect.KClass

interface MultiblockEntityDataFixer<T: MultiblockEntity> : DataFixer {
	val clazz: KClass<T>

	fun fixEntity(entity: T)
}
