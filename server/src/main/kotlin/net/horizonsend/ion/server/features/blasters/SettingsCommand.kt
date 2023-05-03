package net.horizonsend.ion.server.features.blasters

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Values
import net.horizonsend.ion.common.Colors.ColorSafety.ACCEPTABLE
import net.horizonsend.ion.common.Colors.ColorSafety.TOO_BRIGHT
import net.horizonsend.ion.common.Colors.ColorSafety.TOO_DARK
import net.horizonsend.ion.common.Colors.calculateColorSafety
import net.horizonsend.ion.common.database.PlayerData
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import org.bukkit.Color
import org.bukkit.entity.Player
import org.jetbrains.exposed.sql.transactions.transaction

@CommandAlias("settings")
class SettingsCommand : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("particle")
	@CommandCompletion("@particles")
	@CommandPermission("ion.settings.particle")
	fun onSettingsParticleCommand(sender: Player, @Values("@particles") particle: String) = transaction {
		PlayerData[sender.uniqueId]?.particle = particle
		sender.information("Set particle to $particle")
	}

	@Suppress("Unused")
	@Subcommand("particle")
	fun onSettingsParticleCommand(sender: Player) = transaction {
		PlayerData[sender.uniqueId]?.particle = null
		sender.information("Cleared particle")
	}

	@Suppress("Unused")
	@Subcommand("color")
	@CommandPermission("ion.settings.color")
	fun onChooseColor(sender: Player, red: Int, green: Int, blue: Int) = transaction {
		// Ensure in range
		@Suppress("NAME_SHADOWING") val red = red.coerceIn(0..255)

		@Suppress("NAME_SHADOWING") val green = green.coerceIn(0..255)

		@Suppress("NAME_SHADOWING") val blue = blue.coerceIn(0..255)

		when (calculateColorSafety(red, green, blue)) {
			TOO_BRIGHT -> sender.userError("Chosen color is too bright!")
			TOO_DARK -> sender.userError("Chosen color is too dark!")
			ACCEPTABLE -> {
				PlayerData[sender.uniqueId]?.color = Color.fromRGB(red, green, blue).asRGB()
				sender.information("Set color to $red $green $blue")
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("color")
	fun onSettingsColorCommand(sender: Player) = transaction {
		PlayerData[sender.uniqueId]?.color = null
		sender.information("Cleared color")
	}
}
