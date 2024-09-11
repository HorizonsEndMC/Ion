package net.horizonsend.ion.server.features.multiblock.entity.type.power

import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.transport.grid.PowerGrid
import net.horizonsend.ion.server.features.transport.grid.util.Sink
import net.horizonsend.ion.server.features.transport.node.power.PowerInputNode
import net.horizonsend.ion.server.features.world.chunk.IonChunk
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import org.bukkit.persistence.PersistentDataType

interface PoweredMultiblockEntity: Sink {
	val position: BlockKey
	var powerUnsafe: Int
	val maxPower: Int

	fun setPower(amount: Int) {
		val correctedPower = amount.coerceIn(0, maxPower)

		powerUnsafe = correctedPower

		updatePowerVisually()
	}

	fun isEmpty() = getPower() <= 0
	fun isFull() = getPower() >= maxPower

	fun getPower(): Int {
		return powerUnsafe
	}

	/**
	 * Returns the amount of power that could not be added
	 **/
	fun addPower(amount: Int): Int {
		val newAmount = getPower() + amount

		setPower(newAmount)

		return if (newAmount > maxPower) maxPower - newAmount else 0
	}

	/**
	 * Returns the amount of power that could not be removed
	 **/
	fun removePower(amount: Int): Int {
		val newAmount = getPower() - amount

		setPower(newAmount)

		return if (newAmount < 0) newAmount else 0
	}

	/**
	 * Returns whether this multiblock has the capacity to fit the specified amount of power
	 **/
	fun canFitPower(amount: Int): Boolean {
		return getPower() + amount < maxPower
	}

	/**
	 * Returns true if this amount of power can be removed without reaching zero.
	 **/
	fun canRemovePower(amount: Int): Boolean {
		return getPower() - amount > 0
	}

	companion object {
		private val prefixComponent = text("E: ", NamedTextColor.YELLOW)
	}

	/** Store power data */
	fun storePower(store: PersistentMultiblockData) {
		store.addAdditionalData(NamespacedKeys.POWER, PersistentDataType.INTEGER, powerUnsafe)
	}

	fun updatePowerVisually()

	fun formatPower(): Component = ofChildren(prefixComponent, text(powerUnsafe, GREEN))

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
		existing.grid.registerSink(this)
	}

	fun releaseInputNode() {
		val existing = getInputNode() ?: return
		if (existing.boundMultiblockEntity != this) return

		existing.boundMultiblockEntity = null
		existing.grid.removeSink(this)
	}

	/**
	 * Returns the grid that this multiblock is tied to
	 *
	 * Should only return null if the multiblock is partially unloaded, or not intact.
	 **/
	fun getGrid(): PowerGrid? {
		return getInputNode()?.grid as? PowerGrid
	}

	override fun isRequesting(): Boolean {
		return getPower() < maxPower
	}
}
