package net.horizonsend.ion.server.features.world.data.fixers.multiblock

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.features.world.data.ChunkDataFixer
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign

object MultiblockEntityInitializer : ChunkDataFixer {
	override fun fix(chunk: IonChunk) = Tasks.async {
		chunk.iterateBlocks {
			val data = it.blockData
			if (data !is WallSign) return@iterateBlocks

			Tasks.sync {
				val state = it.state
				if (state !is Sign) return@sync

				val multiblock = MultiblockAccess.getFast(state)
				if (multiblock !is EntityMultiblock<*>) return@sync

				val multiblockDirection = state.getFacing().oppositeFace

				val (x, y, z) = Vec3i(state.x, state.y, state.z).getRelative(multiblockDirection)

				val entityPresent = MultiblockEntities.getMultiblockEntity(chunk.world, x, y, z) != null
				if (entityPresent) return@sync

				MultiblockEntities.setMultiblockEntity(state.world, x, y, z) { manager ->
					multiblock.createEntity(
						manager,
						PersistentMultiblockData(x, y, z, multiblock, multiblockDirection),
						state.world,
						x,
						y,
						z,
						multiblockDirection
					)
				}
			}
		}
	}

	override val dataVersion: Int = 1
}
