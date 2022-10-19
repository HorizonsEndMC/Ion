package net.horizonsend.ion.core.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.starlegacy.PLUGIN
import org.bukkit.command.CommandSender

@CommandAlias("graceperiodtoggle")
@CommandPermission("graceperiodtoggle")
object GracePeriod : BaseCommand() {
	private val graceperiodfile = PLUGIN.dataFolder.resolve("graceperiod")

	val isGracePeriod get() = graceperiodfile.exists()

	@Default
	@Suppress("unused", "unused_parameter")
	fun onToggle(sender: CommandSender) {
		if (isGracePeriod) graceperiodfile.delete()
		else graceperiodfile.createNewFile()
	}
}