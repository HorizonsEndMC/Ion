package net.horizonsend.ion.server.listeners.bukkit

import net.starlegacy.database.schema.starships.PlayerStarshipData
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldLoadEvent
import org.litote.kmongo.eq

class WorldLoadListener : Listener {
	@EventHandler
	fun onWorldLoadEvent(event: WorldLoadEvent) {
		// Missing Ship Purge
		var shipsRemoved = 0
		var chunksRemaining = 0

		for (starship in PlayerStarshipData.find(PlayerStarshipData::levelName eq event.world.name)) {
			val x = blockKeyX(starship.blockKey)
			val y = blockKeyY(starship.blockKey)
			val z = blockKeyZ(starship.blockKey)

			val location = Location(event.world, x.toDouble(), y.toDouble(), z.toDouble())

			event.world.getChunkAtAsync(location, false) { chunk ->
				val cX = x.rem(16)
				val cY = y.rem(16)
				val cZ = z.rem(16)

				if (chunk.getBlock(cX, cY, cZ).type != Material.JUKEBOX) {
					println("Removed missing ${starship.starshipType} at $cX, $cY, $cZ @ ${event.world.name}.")
					PlayerStarshipData.remove(starship._id)
					shipsRemoved++
				}

				chunk.isForceLoaded = false
				chunksRemaining--

				if (chunksRemaining == 0) {
					println("$shipsRemoved missing ships were removed.")
				}
			}

			chunksRemaining++
		}
	}
}