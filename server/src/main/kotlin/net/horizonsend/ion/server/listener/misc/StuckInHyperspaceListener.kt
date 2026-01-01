package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.starship.LastPilotedStarship
import net.horizonsend.ion.server.features.starship.hyperspace.Hyperspace
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerChangedWorldEvent
import org.bukkit.event.player.PlayerJoinEvent

object StuckInHyperspaceListener : SLEventListener() {
	@EventHandler
	fun onPlayerChangeWorld(event: PlayerChangedWorldEvent) {
		val hyperspaceWorld = Hyperspace.getHyperspaceWorld(event.from) ?: return
		val spaceWorld = Bukkit.getWorld(hyperspaceWorld.name.removeSuffix("_hyperspace")) ?: return // inlining this from the Hyperspace class

		val player = event.player

		// first, we tell the player that they are *not* going to be stuck in space after three minutes
		Tasks.asyncDelay(3600) {
			if (player.world != hyperspaceWorld) return@asyncDelay

			player.sendRichMessage("<gray>Detected Player in Hyperspace: Teleporting into Realspace in <green>two minutes<gray>!")
		}

		// next, we move them out of hyperspace to relative realspace position or where their ship will be at
		Tasks.asyncDelay(6000) {
			// this should work
			if (player.world != hyperspaceWorld) return@asyncDelay

			val currentPlayerPos = player.location
			val realspaceLocation = Location(
				spaceWorld,
				currentPlayerPos.x,
				currentPlayerPos.y,
				currentPlayerPos.z
			)

			// you know how you can tell if the code is written by a human? if the variable names are awful.
			val lastPilotedStarshipOrRealspace = LastPilotedStarship.map.getOrDefault(player.uniqueId, realspaceLocation)
			player.teleportAsync(lastPilotedStarshipOrRealspace)
			player.sendRichMessage("<gray>Successfully sent <green>${player.name} <gray>to <green>Realspace<gray>!")
		}
	}

	@EventHandler
	fun onPlayerJoin(event: PlayerJoinEvent) {
		if (!Hyperspace.isHyperspaceWorld(event.player.world)) return
		val spaceWorld = Hyperspace.getRealspaceWorld(event.player.world)

		val player = event.player
		val hyperspaceLocation = player.location

		player.sendRichMessage("<gray>Detected Player in Hyperspace: Teleporting into Realspace in <green>three minutes<gray>!")

		Tasks.asyncDelay(3600) {
			// this should also work
			if (player.world != hyperspaceLocation.world) return@asyncDelay

			val currentPlayerPos = player.location
			val realspaceLocation = Location(
				spaceWorld,
				currentPlayerPos.x,
				currentPlayerPos.y,
				currentPlayerPos.z
			)

			val lastPilotedStarshipOrRealspace = LastPilotedStarship.map.getOrDefault(player.uniqueId, realspaceLocation)
			player.teleportAsync(lastPilotedStarshipOrRealspace)
			player.sendRichMessage("<gray>Successfully sent <green>${player.name} <gray>to <green>Realspace<gray>!")
		}
	}
}
