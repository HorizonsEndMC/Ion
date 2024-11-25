package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.CRUISER_REACTOR_CORE

object CruiserReactorMultiblock : AbstractReactorCore({ customBlock(CRUISER_REACTOR_CORE) }) {
	override val name: String = "cruiserreactor"

	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Cruiser",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}

