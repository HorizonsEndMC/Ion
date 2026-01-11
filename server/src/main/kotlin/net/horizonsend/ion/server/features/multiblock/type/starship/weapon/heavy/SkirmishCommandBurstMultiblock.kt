package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.SkirmishCommandBurstSubsystem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Sign

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

	override fun createSubsystem(starship: Starship, sign: Sign, multiblock: Multiblock): AbstractCommandBurstSubsystem<*> {
		return SkirmishCommandBurstSubsystem(starship, sign, this)
	}
}
