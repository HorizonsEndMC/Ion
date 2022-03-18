package net.horizonsend.ion

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import org.bukkit.command.CommandSender
import org.spongepowered.configurate.ConfigurateException

@CommandAlias("ionreload")
@CommandPermission("ion.reload")
class IonReloadCommand(private val plugin: Ion) : BaseCommand() {
	@Default
	@Description("Reloads Ion's Configuration")
	@Suppress("unused") // Entrypoint (Command)
	fun onIonReload(source: CommandSender) {
		source.sendMiniMessage("<gray>Reloading Ion configuration.")

		try {
			plugin.loadConfiguration()
		} catch (exception: ConfigurateException) {
			source.sendMiniMessage("<red>Failed to load Ion configuration, old configuration remains in use.\n${exception.message}")
			return
		}

		source.sendMiniMessage("<aqua>Configuration reloaded.")
	}
}