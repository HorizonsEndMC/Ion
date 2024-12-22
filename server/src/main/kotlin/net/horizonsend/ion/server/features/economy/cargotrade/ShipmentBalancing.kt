package net.horizonsend.ion.server.features.economy.cargotrade

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.configuration.Configuration
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles.sharedDataFolder

lateinit var balancing: ShipmentBalancing.ShipmentBalancingConfig

object ShipmentBalancing : IonServerComponent() {
	@Serializable
	data class ShipmentBalancingConfig(
		@Serializable
		val generator: GeneratorSection = GeneratorSection(),
		val importExport: ImportExportSection = ImportExportSection(),
		val settlementProfits: SettlementProfitSection = SettlementProfitSection()
	) {
		@Serializable
		data class GeneratorSection(
			val shipmentsPerCity: Int = 9,
			val regenerateIntervalMinutes: Int = 30,
			val marketFactorMin: Double = 1.0,
			val marketFactorMax: Double = 1.15,
			val profitFactorMin: Double = 2.5,
			val profitFactorMax: Double = 3.5,
			val prices: Map<String, Double> = mapOf(),
			val defaultPrice: Double = 50.0,
			val settlementCityChance: Double = 0.15,
			val npcCityChance: Double = 0.1,
			val minExpireDays: Int = 1,
			val maxExpireDays: Int = 6,
			val routeValueExponent: Double = 2.0,
			val minShipmentSize: Int = 1,
			val maxShipmentSize: Int = 219
		)

		@Serializable
		data class ImportExportSection(
			val baseCrateXP: Double = 2.0,
			val minXPFactor: Double = 0.5,
			val maxXPFactor: Double = 1.5
		)

		@Serializable
		data class SettlementProfitSection(
			val creditsPortion: Double = 0.05,
			val xp: XPSection = XPSection()
		) {
			@Serializable
			data class XPSection(
				val sameTerritoryPortion: Double = 0.3,
				val samePlanetPortion: Double = 0.2,
				val defaultPortion: Double = 0.1
			)
		}
	}

	override fun onEnable() = reload()

	fun reload() {
		balancing = Configuration.load(sharedDataFolder, "shipment_balancing.json")
	}
}
