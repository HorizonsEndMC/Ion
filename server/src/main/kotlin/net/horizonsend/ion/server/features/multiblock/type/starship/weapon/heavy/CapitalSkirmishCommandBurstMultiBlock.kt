package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.checklist

import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCapitalCommandBurst
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.AbstractCommandBurst
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material

object CapitalSkirmishCommandBurstMultiBlock : AbstractCapitalCommandBurst({ Material.MAGMA_BLOCK }) {
	override val displayName: Component get() = text("Capital Skirmish Burst")
	override val description: Component get() = text("AOE Speed Pulse")

	override val name: String = "skirmburst"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Capital Skirmish",
		"&7&cCommand Burst&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
