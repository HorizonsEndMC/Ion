package net.horizonsend.ion.server.features.multiblock.entity.linkages

import net.horizonsend.ion.server.features.multiblock.manager.ShipMultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

class ShipLinkageManager(private val shipMultiblockManager: ShipMultiblockManager) : MultiblockLinkageManager() {
	override var linkages: ConcurrentHashMap<BlockKey, LinkageHolder> = ConcurrentHashMap()

	fun displace(movement: StarshipMovement) {
		for ((_, holder) in linkages) {
			holder.displace(movement)
		}
	}
}
