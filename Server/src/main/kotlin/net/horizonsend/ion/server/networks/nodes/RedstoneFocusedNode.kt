package net.horizonsend.ion.server.networks.nodes

import org.bukkit.Material

class RedstoneFocusedNode : FocusedNode() {
	override val companion: AbstractNodeCompanion<RedstoneFocusedNode> = Companion

	companion object : AbstractNodeCompanion<RedstoneFocusedNode>(Material.REDSTONE_BLOCK) {
		override fun construct(): RedstoneFocusedNode = RedstoneFocusedNode()
	}
}