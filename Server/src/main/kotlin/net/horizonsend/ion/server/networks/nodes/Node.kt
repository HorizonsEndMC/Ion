package net.horizonsend.ion.server.networks.nodes

import net.minecraft.world.level.block.Blocks

class Node : AbstractNode() {
	companion object : AbstractNodeCompanion<Node>(Blocks.SPONGE) {
		override fun construct(): Node = Node()
	}
}