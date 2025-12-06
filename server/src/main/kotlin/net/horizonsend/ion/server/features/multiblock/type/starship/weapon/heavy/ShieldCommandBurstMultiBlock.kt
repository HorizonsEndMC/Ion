package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCommandBurst
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material

object ShieldCommandBurstMultiBlock : AbstractCommandBurst({ Material.SEA_LANTERN }) {
	override val displayName: Component get() = text("Shield Burst")
	override val description: Component get() = text("AOE Shield Pulse")

	override val name: String = "shieldburst"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Shield",
		"&7&cCommand Burst&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
