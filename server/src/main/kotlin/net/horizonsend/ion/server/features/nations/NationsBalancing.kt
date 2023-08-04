package net.horizonsend.ion.server.features.nations

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.sharedDataFolder
import net.horizonsend.ion.server.miscellaneous.utils.loadConfig

lateinit var NATIONS_BALANCE: NationsBalancing.Config

object NationsBalancing : IonServerComponent() {
	data class Config(
		val settlement: Settlements = Settlements(),
		val nation: Nations = Nations(),
		val capturableStation: Stations = Stations()
	) {
		data class Settlements(
			val activityDays: Int = 6,
			val cityHourlyTax: Int = 125,
			val cityMinActive: Int = 4,
			val hourlyActivityCredits: Int = 1,
			val inactivityDays: Int = 30,
			val minCreateLevel: Int = 3,
			val renameCost: Int = 1500,
			val maxTaxPercent: Int = 12
		)

		data class Nations(
			val minCreateLevel: Int = 12,
			val minJoinLevel: Int = 7,
			val hourlyActivityCredits: Int = 4,
			val createCost: Int = 20000,
			val renameCost: Int = 3000,
			val costPerSpaceStationBlock: Double = 0.5
		)

		data class Stations(
			val radius: Int = 500,
			val siegeMinDuration: Long = 25,
			val siegeMaxDuration: Long = 60,
			val siegeCost: Int = 1000,
			val daysPerSiege: Double = 0.5,
			val siegerXP: Int = 2000,
			val siegerAllyXP: Int = 1000
		)
	}

	override fun onEnable() {
		reload()
	}

	fun reload() {
		NATIONS_BALANCE = loadConfig(sharedDataFolder, "nations_balancing")
	}

}
