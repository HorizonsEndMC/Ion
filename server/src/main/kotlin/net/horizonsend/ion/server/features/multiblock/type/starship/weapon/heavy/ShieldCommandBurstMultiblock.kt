package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.starship.Starship
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.AbstractCommandBurstSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.command_burst.ShieldCommandBurstSubsystem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Sign

object ShieldCommandBurstMultiblock : AbstractCommandBurstMultiblock(Material.SEA_LANTERN) {
	override val displayName: Component get() = text("Shield Burst")
	override val description: Component get() = text("AOE Shield Pulse")

	override val name: String = "shieldburst"
	override val signText = createSignText(
		"&7-=[&c==&a==&b==&7]=-",
		"&0Shield",
		"&7&cCommand Burst&7",
		"&7-=[&c==&a==&b==&7]=-"
	)

	override fun createSubsystem(starship: Starship, sign: Sign, multiblock: Multiblock): AbstractCommandBurstSubsystem<*> {
		return ShieldCommandBurstSubsystem(starship, sign, this)
	}
}
