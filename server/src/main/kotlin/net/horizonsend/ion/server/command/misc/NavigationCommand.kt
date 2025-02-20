package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.gui.custom.navigation.NavigationGalacticMapGui
import net.horizonsend.ion.server.features.gui.custom.navigation.NavigationSystemMapGui
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import org.bukkit.entity.Player

@CommandAlias("navigation|nav")
object NavigationCommand : SLCommand() {
	@Default
	@Suppress("unused")
	fun onOpenNavigationGui(sender: Player) {
		openNavigationGui(sender)
	}

	fun openNavigationGui(player: Player) {
		val currentWorld = player.world
		val foundWorld = if (!currentWorld.hasFlag(WorldFlag.SPACE_WORLD)) {
			// Assume the player is on a planet; get the space world the planet is in
			Space.getPlanet(currentWorld)?.spaceWorld
		} else currentWorld
		if (foundWorld != null) {
			NavigationSystemMapGui(player, foundWorld).openMainWindow()
		} else {
			NavigationGalacticMapGui(player).openMainWindow()
		}
	}
}
