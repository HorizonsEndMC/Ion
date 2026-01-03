package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material

object SkirmishCommandBurstMultiblock : AbstractCommandBurstMultiblock(Material.MAGMA_BLOCK) {
	override val displayName: Component get() = text("Skirmish Burst")
	override val description: Component get() = text("AOE Speed Pulse")

	override val name: String = "skirmburst"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Skirmish",
		"&7&cCommand Burst&7",
		"&7-=[&c==&a==&b==&7]=-"
	)
}
