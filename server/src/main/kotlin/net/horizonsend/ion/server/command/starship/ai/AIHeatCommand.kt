package net.horizonsend.ion.server.command.starship.ai

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.reward.AIKillStreak
import org.bukkit.entity.Player

@CommandPermission("ion.aiheat")
@CommandAlias("aiheat")
object AIHeatCommand : SLCommand() {
	@Default
	fun onAIHeat(sender: Player, level: Int) {
		if (level !in 0..20) {
			fail { "Heat level must be between 0 and 20." }
		}

		sender.success("Setting heat to $level")
		AIKillStreak.setHeatLevel(sender, level)
	}
}
