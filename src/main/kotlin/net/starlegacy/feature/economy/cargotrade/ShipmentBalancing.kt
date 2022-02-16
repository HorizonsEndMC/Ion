package net.starlegacy.feature.economy.cargotrade

import net.starlegacy.SLComponent
import net.starlegacy.util.loadConfig

lateinit var balancing: ShipmentBalancing.ShipmentBalancingConfig

object ShipmentBalancing : SLComponent() {
	data class ShipmentBalancingConfig(
		val generator: GeneratorSection = GeneratorSection(),
		val importExport: ImportExportSection = ImportExportSection(),
		val settlementProfits: SettlementProfitSection = SettlementProfitSection()
	) {
		data class GeneratorSection(
			val shipmentsPerCity: Int = 9,
			val regenerateIntervalMinutes: Int = 30,
			val marketFactorMin: Double = 1.0,
			val marketFactorMax: Double = 1.15,
			val profitFactorMin: Double = 2.5,
			val profitFactorMax: Double = 3.5,
			val prices: Map<String, Double> = mapOf(),
			val defaultPrice: Double = 50.0,
			val settlementCityChance: Double = 1.0,
			val npcCityChance: Double = 0.1,
			val minExpireDays: Int = 1,
			val maxExpireDays: Int = 6,
			val routeValueExponent: Double = 2.0,
			val minShipmentSize: Int = 1,
			val maxShipmentSize: Int = 219
		)

		data class ImportExportSection(
			val baseCrateXP: Double = 2.0,
			val minXPFactor: Double = 0.5,
			val maxXPFactor: Double = 1.5
		)

		data class SettlementProfitSection(
			val creditsPortion: Double = 0.05,
			val xp: XPSection = XPSection()
		) {
			data class XPSection(
				val sameTerritoryPortion: Double = 0.3,
				val samePlanetPortion: Double = 0.2,
				val defaultPortion: Double = 0.1
			)
		}
	}


	override fun onEnable() = reload()

	fun reload() {
		balancing = loadConfig(plugin.sharedDataFolder, "shipment_balancing")
	}
}
