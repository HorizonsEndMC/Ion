package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object FauxReactorMultiblock : AbstractReactorCore({ netheriteBlock() }) {
	override val displayName: Component get() = text("AI Reactor")
	override val description: Component get() = text("Reactor core critical to a AI Supercapital's functionality.")

	override val name: String = "cruiserreactor"

	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0AI",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
