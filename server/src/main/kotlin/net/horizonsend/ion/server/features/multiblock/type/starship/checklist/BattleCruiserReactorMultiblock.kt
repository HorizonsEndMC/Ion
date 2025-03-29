package net.horizonsend.ion.server.features.multiblock.type.starship.checklist

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

object BattleCruiserReactorMultiblock : AbstractReactorCore({ customBlock(CustomBlockKeys.BATTLECRUISER_REACTOR_CORE.getValue()) }) {
	override val displayName: Component get() = text("Battlecruiser Reactor")
	override val description: Component get() = text("Reactor core critical to a Battlecruiser's functionality.")

	override val name: String = "bcreactor"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Battlecruiser",
		"&7&cFusion Reactor&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}

