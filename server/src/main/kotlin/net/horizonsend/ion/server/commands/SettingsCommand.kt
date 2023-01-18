package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import co.aikar.commands.annotation.Values
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.update
import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.extensions.sendUserError
import org.bukkit.Color
import org.bukkit.entity.Player
import kotlin.math.pow

@CommandAlias("settings")
class SettingsCommand : BaseCommand() {
	@Suppress("Unused")
	@Subcommand("particle")
	@CommandCompletion("@particles")
	@CommandPermission("ion.settings.particle")
	fun onSettingsParticleCommand(sender: Player, @Values("@particles") particle: String) {
		PlayerData[sender.uniqueId].update { this.particle = particle }
		sender.sendInformation("Set particle to $particle")
	}

	@Suppress("Unused")
	@Subcommand("particle")
	fun onSettingsParticleCommand(sender: Player) {
		PlayerData[sender.uniqueId].update { this.particle = null }
		sender.sendInformation("Cleared particle")
	}

	@Suppress("Unused")
	@Subcommand("color")
	@CommandPermission("ion.settings.color")
	fun onChooseColor(sender: Player, red: Int, green: Int, blue: Int) {
		// Ensure in range
		@Suppress("NAME_SHADOWING") val red = red.coerceIn(0..255)

		@Suppress("NAME_SHADOWING") val green = green.coerceIn(0..255)

		@Suppress("NAME_SHADOWING") val blue = blue.coerceIn(0..255)

		// We get the perceived lightness to prevent people from selecting colors which are hard to see.
		// https://stackoverflow.com/questions/56678483
		val decimalRed = red / 255.0
		val decimalGreen = green / 255.0
		val decimalBlue = blue / 255.0

		val linearRed = if (decimalRed <= 0.04045) decimalRed / 12.92 else ((decimalRed + 0.055) / 1.055).pow(2.4)
		val linearGreen = if (decimalGreen <= 0.04045) decimalGreen / 12.92 else ((decimalGreen + 0.055) / 1.055).pow(2.4)
		val linearBlue = if (decimalBlue <= 0.04045) decimalBlue / 12.92 else ((decimalBlue + 0.055) / 1.055).pow(2.4)

		val luminance = 0.2126 * linearRed + 0.7152 * linearGreen + 0.0722 * linearBlue
		val perceivedLightness = if (luminance <= (216.0 / 24389.0)) luminance * (24389.0 / 27.0) else luminance.pow(1.0 / 3.0) * 116 - 16

		if (perceivedLightness < 50.0) {
			sender.sendUserError("This color is too dark, please choose something lighter.")
			return
		}

		PlayerData[sender.uniqueId].update { color = Color.fromRGB(red, green, blue).asRGB() }
		sender.sendInformation("Set color to $red $green $blue")
	}

	@Suppress("Unused")
	@Subcommand("color")
	fun onSettingsColorCommand(sender: Player) {
		PlayerData[sender.uniqueId].update { color = null }
		sender.sendInformation("Cleared color")
	}
}
