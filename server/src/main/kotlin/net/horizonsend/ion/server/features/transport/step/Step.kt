package net.horizonsend.ion.server.features.transport.step

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.highlightBlock
import net.horizonsend.ion.server.features.transport.container.ResourceContainer
import net.horizonsend.ion.server.features.transport.grid.TransportNetwork
import net.horizonsend.ion.server.features.transport.node.GridNode
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.debugAudience
import org.bukkit.block.BlockFace

abstract class Step(
	open val transportNetwork: TransportNetwork,
	open val origin: ResourceContainer<*>?,
	var direction: BlockFace,
	var currentNode: GridNode
) {
	var status: TransferStatus = TransferStatus.STEPPING

	var children = mutableSetOf<Step>()

	var depth: Int = 0

	fun step() {
		depth++

		if (depth >= MAX_STEP_DEPTH) return

		debugAudience.highlightBlock(Vec3i(currentNode.x, currentNode.y, currentNode.z), 50L)

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
