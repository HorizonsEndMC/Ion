package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.DBVec3i
import net.horizonsend.ion.common.utils.NavigationObject
import net.horizonsend.ion.server.configuration.util.Pos
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip.SerializableDealerShipInformation
import net.horizonsend.ion.server.features.world.WorldSettings
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import java.time.DayOfWeek

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val crossServerDeathMessages: Boolean = false,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(),
	val soldShips: List<SerializableDealerShipInformation> = listOf(),
	val dutyModeMonitorWebhook: String? = null,
	val eventLoggerWebhook: String? = null,
	val getPosMaxRange: Double = 600.0,
	val nearMaxRange: Double = 1200.0,
	val restartHour: Int = 8,
	val globalCustomSpawns: List<WorldSettings.SpawnedMob> = listOf(),
	val worldResetSettings: AutoWorldReset = AutoWorldReset(),
	val rentalZoneCollectionDay: DayOfWeek = DayOfWeek.SUNDAY,
	val deleteInvalidMultiblockData: Boolean = false,
	val pastebinApiDevKey: String? = null,
	val tutorialEscapePodShip: SerializableDealerShipInformation = SerializableDealerShipInformation(
		price = 0.0,
		schematicName = "TutorialEscapePod",
		guiMaterial = Material.SPONGE,
		displayName = "",
		cooldown = 0L,
		protectionCanBypass = true,
		shipClass = StarshipType.SHUTTLE.name,
		lore = listOf(),
		pilotOffset = Vec3i(0, 0, 6)
	),
	val tutorialOrigin: DBVec3i = Vec3i(93, 359, 82)
) {
	@Serializable
	data class HyperspaceBeacon(
		override val name: String,
		val radius: Double,
		val spaceLocation: Pos,
		val destination: Pos,
		val destinationName: String? = null,
		val exits: ArrayList<Pos>? = null,
		val prompt: String? = null
	) : NavigationObject

	@Serializable
	data class AutoWorldReset(
		val worldResetDay: DayOfWeek = DayOfWeek.WEDNESDAY,
		val worldResetDirectories: List<String> = listOf()
	)
}
