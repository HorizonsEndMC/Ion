package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.custom.navigation.NavigationSystemMapGui
import org.bukkit.entity.Player

@CommandAlias("navigation|nav")
object NavigationCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onOpenNavigationGui(sender: Player) {
		NavigationSystemMapGui(sender, sender.world).openMainWindow()
	}
}
