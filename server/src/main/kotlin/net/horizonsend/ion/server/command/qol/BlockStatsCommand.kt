package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.starship.FLYABLE_BLOCKS
import net.horizonsend.ion.server.features.starship.Mass
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import org.bukkit.Material
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.Player

@CommandAlias("blastresistance|blockstats|blockinfo")
object BlockStatsCommand : SLCommand() {
	@Default
	@CommandCompletion("")
	fun command(player: Player, material: Material) {
		player.sendMessage(ofChildren(
			text("Flyable", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(FLYABLE_BLOCKS.contains(material), AQUA), newline(),
			text("Blast Resistance", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(CraftMagicNumbers.getBlock(material).explosionResistance, AQUA), newline(),
			text("Mass", HE_MEDIUM_GRAY), text(": ", HE_DARK_GRAY), text(Mass[material], AQUA)
		))
	}
}
