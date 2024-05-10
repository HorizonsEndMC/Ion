package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.data.DataFixer

object MultiblockEntityFixer : DataFixer {
	private val fixers: MutableList<DataUpgrader<out MultiblockEntity>> = mutableListOf()

	private fun <T: MultiblockEntity> addDataUpgrader(name: String, dataVersion: Int, apply: T.() -> Unit) {
		fixers.add(DataUpgrader(name, dataVersion, apply))
	}

	override fun registerFixers() {
		// Don't need any yet
	}

	private class DataUpgrader<T : MultiblockEntity>(@Suppress("unused") val name: String, val dataVersion: Int, private val applicator: T.() -> Unit) {
		fun apply(multiblock: T) = applicator.invoke(multiblock)
	}

	fun upgrade(chunk: IonChunk) {
		chunk.multiblockManager.getAllMultiblockEntities().values.forEach {
			upgrade(it, chunk.dataVersion)
		}
	}

	fun <T: MultiblockEntity> upgrade(multiblockEntity: T, dataVersion: Int) {
		val fixersFor = fixers
			.filterIsInstance<DataUpgrader<T>>()
			.filter { it.dataVersion > dataVersion }

		fixersFor.forEach { it.apply(multiblockEntity) }
	}
}
