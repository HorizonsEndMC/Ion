package net.starlegacy.feature.nations

import net.starlegacy.SLComponent
import net.starlegacy.sharedDataFolder
import net.starlegacy.util.loadConfig

lateinit var NATIONS_BALANCE: NationsBalancing.Config

object NationsBalancing : SLComponent() {
	data class Config(
		val settlement: Config.Settlements = Settlements(),
		val nation: Config.Nations = Nations(),
		val capturableStation: Config.Stations = Stations()
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
			val minCreateLevel: Int = 2,
			val minJoinLevel: Int = 1,
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

	override fun supportsVanilla(): Boolean {
		return true
	}
}