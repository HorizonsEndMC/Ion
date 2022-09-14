package net.horizonsend.ion.server.networks.nodes

import org.bukkit.Material

class IronFocusedNode : FocusedNode() {
	override val companion: AbstractNodeCompanion<IronFocusedNode> = Companion

	companion object : AbstractNodeCompanion<IronFocusedNode>(Material.IRON_BLOCK) {
		override fun construct(): IronFocusedNode = IronFocusedNode()
	}
}