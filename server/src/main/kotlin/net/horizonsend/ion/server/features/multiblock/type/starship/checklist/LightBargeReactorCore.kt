package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.LIGHT_BARGE_REACTOR_CORE
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object LightBargeReactorMultiblock : AbstractReactorCore({ customBlock(LIGHT_BARGE_REACTOR_CORE) }) {
	override val displayName: Component get() = text("Light Barge Reactor")
	override val description: Component get() = text("Reactor core critical to a Light Barge's functionality.")

	override val name: String = "lbargereactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Light Barge",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
