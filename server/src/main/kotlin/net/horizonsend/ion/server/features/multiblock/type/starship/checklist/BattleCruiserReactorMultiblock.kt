package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.BATTLECRUISER_REACTOR_CORE

object BattleCruiserReactorMultiblock : AbstractReactorCore({ customBlock(BATTLECRUISER_REACTOR_CORE) }) {
	override val name: String = "bcreactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Battlecruiser",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}

