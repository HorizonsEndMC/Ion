package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object CruiserReactorMultiblock : AbstractReactorCore({ customBlock(CustomBlockKeys.CRUISER_REACTOR_CORE.getValue()) }) {
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

