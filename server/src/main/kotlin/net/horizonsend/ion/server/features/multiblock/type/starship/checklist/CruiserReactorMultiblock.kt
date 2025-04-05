package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.CRUISER_REACTOR_CORE
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object CruiserReactorMultiblock : AbstractReactorCore({ customBlock(CRUISER_REACTOR_CORE) }) {
	override val displayName: Component get() = text("Cruiser Reactor")
	override val description: Component get() = text("Reactor core critical to a Cruiser's functionality.")

	override val name: String = "cruiserreactor"

	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Cruiser",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}

