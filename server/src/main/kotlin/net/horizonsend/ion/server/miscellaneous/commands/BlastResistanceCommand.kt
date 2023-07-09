package net.horizonsend.ion.server.miscellaneous.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R3.util.CraftMagicNumbers
import org.bukkit.entity.Player

@CommandAlias("blastresistance")
class BlastResistanceCommand : BaseCommand() {
	@Default
	@CommandCompletion("")
	fun command(player: Player, material: Material) {
		player.success("The explosion resistance of $material is: ${CraftMagicNumbers.getBlock(material).explosionResistance}")
	}
}
