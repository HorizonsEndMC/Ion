package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.transport.container.ResourceContainer
import net.horizonsend.ion.server.features.transport.grid.ChunkTransportNetwork
import org.bukkit.block.BlockFace

abstract class Step(
	open val transportNetwork: ChunkTransportNetwork,
	open val origin: ResourceContainer<*>?,
	var direction: BlockFace,
) {
	var status: TransferStatus = TransferStatus.STEPPING

	var children = mutableSetOf<Step>()

	var depth: Int = 0

	fun step() {
		depth++

		if (depth >= MAX_STEP_DEPTH) return

		when (status) {
			TransferStatus.COMPLETE -> return
			TransferStatus.BLOCKED -> return
			TransferStatus.SPLIT -> stepChildren()
			TransferStatus.STEPPING -> step()
		}
	}

	private fun stepChildren() {
		val iterator = children.iterator()

		while (iterator.hasNext()) {
			val child = iterator.next()

			// Remove finished children
			if (child.status == TransferStatus.COMPLETE || child.status == TransferStatus.BLOCKED) {
				iterator.remove()

				continue
			}

			child.step()
		}
	}

	companion object {
		const val MAX_STEP_DEPTH = 25
	}
}
