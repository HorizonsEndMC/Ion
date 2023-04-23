package net.horizonsend.ion.server.migrator

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.DATA_VERSION
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.player.PlayerLoginEvent
import org.bukkit.event.world.ChunkLoadEvent
import org.bukkit.event.world.WorldInitEvent
import org.bukkit.persistence.PersistentDataHolder
import org.bukkit.persistence.PersistentDataType.INTEGER

object DataMigrator : Listener {
	const val LATEST_VERSION = 1

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerLoginEvent(event: PlayerLoginEvent) = event.player.migrate(event.player.name)

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onChunkLoadEvent(event: ChunkLoadEvent) = event.chunk.migrate("${event.chunk.x}, ${event.chunk.z} @ ${event.chunk.world.name}")

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onWorldInitEvent(event: WorldInitEvent) = event.world.migrate(event.world.name)

	private fun PersistentDataHolder.migrate(name: String) {
		val initialVersion = persistentDataContainer.get(DATA_VERSION, INTEGER) ?: 0

		for (currentVersion in initialVersion until LATEST_VERSION) {
			val newVersion = currentVersion + 1

			// TODO: Actually do migrations - Aury @ Astralchroma

			persistentDataContainer.set(DATA_VERSION, INTEGER, newVersion)
			IonServer.slF4JLogger.info("Migrated $name from $currentVersion to $newVersion")
		}
	}
}
