package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.NavigationObject
import net.horizonsend.ion.server.configuration.util.Pos
import net.horizonsend.ion.server.features.starship.dealers.NPCDealerShip
import net.horizonsend.ion.server.features.world.WorldSettings
import java.time.DayOfWeek

@Serializable
data class ServerConfiguration(
	val serverName: String? = null,
	val crossServerDeathMessages: Boolean = false,
	val particleColourChoosingMoneyRequirement: Double? = 5.0,
	val beacons: List<HyperspaceBeacon> = listOf(),
	val soldShips: List<NPCDealerShip.SerializableDealerShipInformation> = listOf(),
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
