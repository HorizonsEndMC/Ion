package net.horizonsend.ion.server.legacy.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.IonServer.Companion.Ion
import org.bukkit.command.CommandSender

@CommandAlias("graceperiodtoggle")
@CommandPermission("graceperiodtoggle")
object GracePeriod : BaseCommand() {
	private val gracePeriodFile = Ion.dataFolder.resolve("gracePeriod")

	var isGracePeriod = gracePeriodFile.exists()
		private set

	@Default
	@Suppress("unused", "unused_parameter")
	fun onToggle(sender: CommandSender) {
		if (isGracePeriod) {
			gracePeriodFile.delete()
		} else {
			gracePeriodFile.createNewFile()
		}

		isGracePeriod = gracePeriodFile.exists()
	}
}
