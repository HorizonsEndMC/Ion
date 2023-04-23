package net.horizonsend.ion.server.migrator.migrators

import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player

interface Migrator {
	fun migratePlayer(player: Player) {}
	fun migrateChunk(chunk: Chunk) {}
	fun migrateWorld(world: World) {}
}
