package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.lineBreakWithCenterText
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawners
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.StatusScheduler
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player


object EncounterStatusCommand : SLCommand() {
	@Suppress("Unused")
	@CommandAlias("encounterstatus")
	@CommandPermission("ion.command.encounterstatus")
	fun onExecute(sender: Player) {
		sender.sendMessage(lineBreakWithCenterText(text("AI Encounters", HEColorScheme.HE_LIGHT_ORANGE)))

		for (spawner in AISpawners.getAllSpawners()) {
			if (spawner.scheduler !is StatusScheduler) continue
			val scheduler = (spawner.scheduler as StatusScheduler)
			val line = scheduler.getStatus()

			sender.sendMessage(line)
		}

		sender.sendMessage(net.horizonsend.ion.common.utils.text.lineBreak(44))
	}
}
