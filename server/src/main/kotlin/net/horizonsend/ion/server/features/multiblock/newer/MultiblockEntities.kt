package net.horizonsend.ion.server.features.multiblock.newer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity

object MultiblockEntities {
	private val multiblockCoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())

	fun <T: MultiblockEntity> T.executeAsync(block: suspend () -> Unit) {
		multiblockCoroutineScope.launch { block.invoke() }
	}
}
