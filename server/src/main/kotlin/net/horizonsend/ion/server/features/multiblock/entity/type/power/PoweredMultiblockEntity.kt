package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity {
	val multiblock: NewPoweredMultiblock<*>
	val storage: PowerStorage

	fun loadStoredPower(data: PersistentMultiblockData): PowerStorage {
		return PowerStorage(data.getAdditionalDataOrDefault(NamespacedKeys.POWER, PersistentDataType.INTEGER, 0), multiblock.maxPower)
	}

	fun savePowerData(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, storage.getPower())
	}

	val powerInputOffset: Vec3i

	fun getRealInputLocation(): Vec3i {
		this as MultiblockEntity
		return getRelative(
			origin = vec3i,
			forwardFace= structureDirection,
			right = powerInputOffset.x,
			up = powerInputOffset.y,
			forward = powerInputOffset.z
		)
	}

	var inputNode: PowerInputNode?

	fun bindInputNode(node: PowerInputNode) {
		if (node.boundMultiblockEntity != null) return

		node.boundMultiblockEntity = this
	}

	fun releaseInputNode() {
		val existing = inputNode ?: return
		if (existing.boundMultiblockEntity != this) return

		existing.boundMultiblockEntity = null
	}
}
