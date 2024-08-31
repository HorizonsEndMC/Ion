package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable

@Serializable
data class GlobalGassesConfiguration(
	val collectorTickInterval: Int = 100,
	val powerPlantConsumption: Int = 250,
	val gasses: Gasses = Gasses()
)

@Serializable
data class Gasses(
	val hydrogen: FuelGasConfiguration = FuelGasConfiguration(2, 150, 1000),
	val nitrogen: FuelGasConfiguration = FuelGasConfiguration(1, 100, 500),
	val methane: FuelGasConfiguration = FuelGasConfiguration(3, 200, 750),
	val oxygen: OxidizerGasConfiguration = OxidizerGasConfiguration(1.0, 1000),
	val chlorine: OxidizerGasConfiguration = OxidizerGasConfiguration(1.5, 500),
	val fluorine: OxidizerGasConfiguration = OxidizerGasConfiguration(2.0, 750),
	val carbonDioxide: GasConfiguration = InertGasConfiguration(1000),
	val helium: GasConfiguration = InertGasConfiguration(500),
) {
	@Serializable
	data class InertGasConfiguration(
		override val maxStored: Int
	) : GasConfiguration

	@Serializable
	data class FuelGasConfiguration(
		val powerPerUnit: Int,
		val cooldown: Int,
		override val maxStored: Int
	) : GasConfiguration

	@Serializable
	data class OxidizerGasConfiguration(
		val powerMultiplier: Double,
		override val maxStored: Int
	) : GasConfiguration

	@Serializable
	sealed interface GasConfiguration {
		val maxStored: Int
	}
}
