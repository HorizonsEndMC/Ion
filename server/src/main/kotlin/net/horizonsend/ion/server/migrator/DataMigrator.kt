package net.horizonsend.ion.server.migrator

import net.horizonsend.ion.server.migrator.migrators.InitialMigrator
import net.horizonsend.ion.server.migrator.migrators.Migrator
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

	val MIGRATORS = arrayOf(
		InitialMigrator, // 0 (None) -> 1 (Initial)
	)

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerLoginEvent(event: PlayerLoginEvent) = event.player.migrate(Migrator::migratePlayer)

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onChunkLoadEvent(event: ChunkLoadEvent) = event.chunk.migrate(Migrator::migrateChunk)

	@Deprecated("Event Listener")
	@EventHandler(priority = EventPriority.LOWEST)
	fun onWorldInitEvent(event: WorldInitEvent) = event.world.migrate(Migrator::migrateWorld)

	private fun <T : PersistentDataHolder> T.migrate(migrate: Migrator.(T) -> Unit) {
		val initialVersion = persistentDataContainer.get(DATA_VERSION, INTEGER) ?: 0

		for (currentVersion in initialVersion until LATEST_VERSION) {
			val newVersion = currentVersion + 1

			MIGRATORS[currentVersion].migrate(this)

			persistentDataContainer.set(DATA_VERSION, INTEGER, newVersion)
		}
	}
}
