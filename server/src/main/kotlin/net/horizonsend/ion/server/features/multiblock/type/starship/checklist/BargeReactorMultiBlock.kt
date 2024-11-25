package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.BARGE_REACTOR_CORE

object BargeReactorMultiBlock : AbstractReactorCore({ customBlock(BARGE_REACTOR_CORE) }) {
	override val name: String = "bargereactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Barge",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
