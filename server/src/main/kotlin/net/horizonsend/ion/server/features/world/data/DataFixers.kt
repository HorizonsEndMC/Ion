package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.data.fixers.multiblock.MultiblockEntityInitializer
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.DATA_VERSION
import org.bukkit.block.Sign
import org.bukkit.persistence.PersistentDataType.INTEGER

object DataFixers: IonServerComponent() {
	private val dataFixers = mutableListOf<DataFixer>()

	private val worldDataFixers = mutableListOf<WorldDataFixer>()
	private val chunkDataFixers = mutableListOf<ChunkDataFixer>()

	private val multiblockEntityDataFixers = mutableListOf<MultiblockEntityDataFixer<*>>()
	private val signDataFixers = mutableListOf<SignDataFixer>()

	private fun registerFixer(fixer: DataFixer) {
		dataFixers.add(fixer)
	}

	private fun registerFixers() {
		registerFixer(MultiblockEntityInitializer)
	}

	override fun onEnable() {
		registerFixers()

		// Sort by data version, ascending
		// Oldest will be applied first until data is up to date
		dataFixers.sortBy { it.dataVersion }

		dataFixers.filterIsInstanceTo(worldDataFixers)
		dataFixers.filterIsInstanceTo(chunkDataFixers)
		dataFixers.filterIsInstanceTo(multiblockEntityDataFixers)
		dataFixers.filterIsInstanceTo(signDataFixers)
	}

	fun handleWorldInit(world: IonWorld) {
		for (worldFixer in worldDataFixers.filter { it.dataVersion > world.dataVersion }) {
			worldFixer.fix(world)

			world.dataVersion = worldFixer.dataVersion
		}
	}

	fun handleChunkLoad(chunk: IonChunk) {
		for (chunkDataFixer in chunkDataFixers.filter { it.dataVersion > chunk.dataVersion }) {
			chunkDataFixer.fix(chunk)

			chunk.dataVersion = chunkDataFixer.dataVersion
		}

		return
		SignFixerEntrance.iterateChunk(chunk)
	}

	fun handleMultiblockSignLoad(sign: Sign) {
		val dataVersion = sign.persistentDataContainer.getOrDefault(DATA_VERSION, INTEGER, 0)

		for (signFixer in signDataFixers.filter { it.dataVersion > dataVersion }) {
			signFixer.fixSign(sign)

			sign.persistentDataContainer.set(DATA_VERSION, INTEGER, signFixer.dataVersion)
			sign.update()
		}
	}
}
