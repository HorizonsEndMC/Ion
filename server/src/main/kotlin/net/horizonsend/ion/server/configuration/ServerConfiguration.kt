package net.horizonsend.ion.server.configuration

import com.sk89q.worldedit.extent.clipboard.Clipboard
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.common.database.StarshipTypeDB
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.util.Pos
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.world.WorldSettings
import net.horizonsend.ion.server.miscellaneous.utils.actualType
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import org.bukkit.Material

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val crossServerDeathMessages: Boolean = false,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(),
	val soldShips: List<Ship> = listOf(),
	val dutyModeMonitorWebhook: String? = null,
	val eventLoggerWebhook: String? = null,
	val getPosMaxRange: Double = 600.0,
	val nearMaxRange: Double = 1200.0,
	val restartHour: Int = 8,
	val globalCustomSpawns: List<WorldSettings.SpawnedMob> = listOf(),
) {
	@Serializable
	data class HyperspaceBeacon(
		val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val exits: ArrayList<Pos>? = null,
		val prompt: String? = null
	)

	/**
	 * @param cooldown in ms
	 **/
	@Serializable
	data class Ship(
		val price: Double,
		val displayName: String,
		val schematicName: String,
		val guiMaterial: Material,
		val cooldown: Long,
		val protectionCanBypass: Boolean,
		private val shipClass: StarshipTypeDB,
		val lore: List<String>
	) {
		val shipType: StarshipType get() = shipClass.actualType

		@Transient
		val schematicFile = IonServer.dataFolder.resolve("sold_ships").resolve("$schematicName.schem")

		fun schematic(): Clipboard = readSchematic(schematicFile)!!
	}
}
