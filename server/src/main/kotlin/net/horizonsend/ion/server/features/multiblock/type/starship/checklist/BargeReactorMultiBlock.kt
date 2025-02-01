package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.BARGE_REACTOR_CORE
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object BargeReactorMultiBlock : AbstractReactorCore({ customBlock(BARGE_REACTOR_CORE) }) {
	override val displayName: Component get() = text("Barge Reactor")
	override val description: Component get() = text("Reactor core critical to a Barge's functionality.")

	override val name: String = "bargereactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Barge",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
