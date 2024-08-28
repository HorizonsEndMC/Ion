package net.horizonsend.ion.server.features.world.data

import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.block.Sign
import org.bukkit.block.data.type.WallSign

object SignFixerEntrance {
	fun iterateChunk(chunk: IonChunk) = Tasks.async {
		chunk.iterateBlocks {
			val data = it.blockData
			if (data !is WallSign) return@iterateBlocks

			Tasks.sync {
				val state = it.state as Sign
				DataFixers.handleMultiblockSignLoad(state)
			}
		}
	}
}
