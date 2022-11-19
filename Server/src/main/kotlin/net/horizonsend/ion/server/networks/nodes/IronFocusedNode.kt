package net.horizonsend.ion.server.networks.nodes

import net.minecraft.world.level.block.Blocks

class IronFocusedNode : FocusedNode() {
	companion object : AbstractNodeCompanion<IronFocusedNode>(Blocks.IRON_BLOCK) {
		override fun construct(): IronFocusedNode = IronFocusedNode()
	}
}