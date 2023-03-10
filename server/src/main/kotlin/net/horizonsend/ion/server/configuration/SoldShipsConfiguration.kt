package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import org.bukkit.Material

@Serializable
data class SoldShipsConfiguration(
	val soldShips: Ships = Ships()
) {
	@Serializable
	data class Ships(
		val shuttle: Ship = Ship(
			200.0,
			"Spawn Shuttle",
			"schematics/HORRIBLE_DoctorXeno_spawn_shuttle.schem",
			Material.JUKEBOX,
			1,
			0.0,
			0.0,
			0.0,
			"",
			"",
			"",
			"",
			"<gray>Spawn Shuttle"
		),
		val miner: Ship = Ship(
			25000.0,
			"Miner Shuttle",
			"schematics/shuttle_miner.schem",
			Material.DIAMOND_PICKAXE,
			1,
			0.0,
			0.0,
			0.0,
			"",
			"",
			"",
			"",
			"<gold>Miner Shuttle"
		)
	) {
		@Serializable
		data class Ship(
			val cost: Double,
			val name: String,
			val pathToSchem: String?,
			val material: Material,
			val cooldown: Long, // ticks
			val teleportOffsetX: Double, // teleport offsets to teleport the player to after the ship is placed (away from schematic origin)
			val teleportOffsetY: Double,
			val teleportOffsetZ: Double,
			val loreLine1: String,
			val loreLine2: String,
			val loreLine3: String,
			val loreLine4: String,
			val displayName: String
		)
	}
}
