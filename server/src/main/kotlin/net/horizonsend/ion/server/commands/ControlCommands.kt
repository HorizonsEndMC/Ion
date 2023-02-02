package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Private
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.extensions.sendHint
import net.horizonsend.ion.server.extensions.sendUserError
import net.horizonsend.ion.server.starships.control.Controller
import net.horizonsend.ion.server.starships.control.NormalController
import net.horizonsend.ion.server.starships.control.LegacyController
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import org.bukkit.craftbukkit.v1_19_R2.entity.CraftPlayer
import org.bukkit.entity.Player

@CommandAlias("control")
class ControlCommands : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("none")
	fun selectNone(sender: Player) = selectControlMode(sender) { null }

	@Suppress("Unused")
	@Subcommand("normal")
	fun selectNormal(sender: Player) = selectControlMode(sender) { NormalController(it, (sender as CraftPlayer).handle) }

	@Suppress("Unused")
	@Subcommand("legacy")
	fun selectLegacy(sender: Player) = selectControlMode(sender) { LegacyController(it, (sender as CraftPlayer).handle) }

	/** Alias for original command, remove this in the future once people stop using it. */
	@Private
	@Suppress("Unused")
	@CommandAlias("dc|directcontrol")
	fun legacySelectDirect(sender: Player) {
		sender.sendHint("/dc is deprecated and may be removed. Use /control normal")
		selectControlMode(sender) {
			if (it.controller is NormalController) {
				LegacyController(it, (sender as CraftPlayer).handle)
			} else {
				NormalController(it, (sender as CraftPlayer).handle)
			}
		}
	}

	/** Alias to avoid repeating the same checks over and over again. */
	private fun selectControlMode(sender: Player, constructor: (ActiveStarship) -> Controller?) {
		val starship = ActiveStarships.findByPilot(sender) ?: run {
			sender.sendUserError("You must be piloting a ship to use this command!")
			return
		}

		starship.controller = constructor(starship)
	}
}
