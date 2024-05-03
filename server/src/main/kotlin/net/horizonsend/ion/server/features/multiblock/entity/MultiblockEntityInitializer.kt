package net.horizonsend.ion.server.features.multiblock.entity

import net.horizonsend.ion.server.features.multiblock.ChunkMultiblockManager
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.features.world.data.DataFixer
import org.bukkit.block.Sign

/**
 * Migrates multiblocks without entities to
 **/
object MultiblockEntityInitializer : DataFixer {
	private val initializers: MutableMap<Multiblock, Initializer<out MultiblockEntity>> = mutableMapOf()

	override fun registerFixers() {

	}

	private class Initializer<T : MultiblockEntity>(@Suppress("unused") val name: String, private val applicator: Sign.() -> T) {
		fun apply(multiblock: Sign): T = applicator.invoke(multiblock)
	}

	fun upgrade(sign: Sign, entityManager: ChunkMultiblockManager) {
		val multiblock = Multiblocks.getFromPDC(sign) ?: return
		val initializer = initializers[multiblock] ?: return

		val multiblockEntity = initializer.apply(sign)
		entityManager.addMultiblockEntity(multiblockEntity)
	}
}
