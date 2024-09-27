package net.horizonsend.ion.server.features.transport.node.type.general

import com.manya.pdc.base.EnumDataType
import net.horizonsend.ion.server.features.transport.node.type.SingleNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys.AXIS
import org.bukkit.block.BlockFace
import org.bukkit.block.BlockFace.NORTH
import org.bukkit.persistence.PersistentDataContainer

abstract class DirectionalNode : SingleNode() {
	lateinit var direction: BlockFace

	override fun loadData(persistentDataContainer: PersistentDataContainer) {
		super.loadData(persistentDataContainer)
		direction = persistentDataContainer.getOrDefault(AXIS, directionPDC, NORTH)
	}

	override fun storeData(persistentDataContainer: PersistentDataContainer) {
		super.storeData(persistentDataContainer)
		persistentDataContainer.set(AXIS, directionPDC, direction)
	}

	companion object {
		private val directionPDC = EnumDataType(BlockFace::class.java)
	}
}
