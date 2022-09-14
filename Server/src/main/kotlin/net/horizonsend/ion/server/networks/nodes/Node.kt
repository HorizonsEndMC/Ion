package net.horizonsend.ion.server.networks.nodes

import org.bukkit.Material

class Node : AbstractNode() {
	override val companion: AbstractNodeCompanion<Node> = Companion

	companion object : AbstractNodeCompanion<Node>(Material.SPONGE) {
		override fun construct(): Node = Node()
	}
}