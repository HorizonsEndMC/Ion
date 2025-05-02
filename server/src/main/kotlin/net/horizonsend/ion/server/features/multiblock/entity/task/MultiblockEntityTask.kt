package net.horizonsend.ion.server.features.multiblock.entity.task

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

interface MultiblockEntityTask<T: MultiblockEntity> {
	val taskEntity: T

	fun disable()

	fun onEnable() {}
	fun onDisable() {}

	fun tick()
}
