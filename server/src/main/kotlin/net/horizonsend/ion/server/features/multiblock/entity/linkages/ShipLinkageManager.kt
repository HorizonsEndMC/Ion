package net.horizonsend.ion.server.features.multiblock.entity.linkages

import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.features.multiblock.manager.ShipMultiblockManager
import net.horizonsend.ion.server.features.starship.movement.StarshipMovement
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import java.util.concurrent.ConcurrentHashMap

class ShipLinkageManager(private val shipMultiblockManager: ShipMultiblockManager) : MultiblockLinkageManager() {
	fun pilot() {
		val old = shipMultiblockManager.world.ion.multiblockManager.linkageManager
		val newKeys = shipMultiblockManager.starship.blocks.mapTo(LongOpenHashSet()) { toBlockKey(blockKeyX(it), blockKeyY(it), blockKeyZ(it)) }
		old.transferTo(newKeys, this)
	}

	fun release() {
		transferTo(shipMultiblockManager.world.ion.multiblockManager.linkageManager)
	}

	override var linkages: ConcurrentHashMap<BlockKey, LinkageHolder> = ConcurrentHashMap()

	fun displace(movement: StarshipMovement) {
		val new: ConcurrentHashMap<BlockKey, LinkageHolder> = ConcurrentHashMap()

		for ((key, holder) in linkages) {
			holder.displace(movement)
			new[movement.displaceKey(key)] = holder
		}

		linkages = new
	}
}
