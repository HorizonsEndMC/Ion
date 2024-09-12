package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.type.NewPoweredMultiblock
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
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

	fun getInputNode(): PowerInputNode? {
		this as MultiblockEntity
		val block = getRealInputLocation()

		val chunk = IonChunk[world, block.x.shr(4), block.z.shr(4)] ?: return null
		val manager = chunk.transportNetwork.powerNodeManager
		val node = manager.getInternalNode(toBlockKey(block))

		if (node != null) return node as? PowerInputNode

		// Try to place unregistered node
		manager.manager.processBlockAddition(world.getBlockAt(block.x, block.y, block.z))
		return manager.getInternalNode(toBlockKey(block)) as? PowerInputNode
	}

	fun bindInputNode() {
		val existing = getInputNode() ?: return
		if (existing.boundMultiblockEntity != null) return

		existing.boundMultiblockEntity = this

	}

	fun releaseInputNode() {
		val existing = getInputNode() ?: return
		if (existing.boundMultiblockEntity != this) return

		existing.boundMultiblockEntity = null
	}
}
