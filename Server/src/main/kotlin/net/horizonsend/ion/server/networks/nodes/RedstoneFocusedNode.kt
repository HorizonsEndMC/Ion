package net.horizonsend.ion.server.networks.nodes

import net.minecraft.world.level.block.Blocks

class RedstoneFocusedNode : FocusedNode() {
	companion object : AbstractNodeCompanion<RedstoneFocusedNode>(Blocks.REDSTONE_BLOCK) {
		override fun construct(): RedstoneFocusedNode = RedstoneFocusedNode()
	}
}