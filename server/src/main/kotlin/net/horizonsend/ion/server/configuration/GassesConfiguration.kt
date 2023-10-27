package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor

@Serializable
data class GassesConfiguration(
	val collectorAmount: Int = 75,
	val collectorMinTime: Long = 250L,
	val collectorVariableTime: Long = 500L,
	val powerPlantConsumption: Int = 30,
	val gasses: Gasses = Gasses()
)

@Serializable
data class Gasses(
	val hydrogen: FuelGasConfiguration = FuelGasConfiguration(2, 150, 1000, listOf("")),
	val nitrogen: FuelGasConfiguration = FuelGasConfiguration(1, 100, 500, listOf("")),
	val methane: FuelGasConfiguration = FuelGasConfiguration(3, 200, 750, listOf("")),
	val oxygen: OxidizerGasConfiguration = OxidizerGasConfiguration(1.0, 1000, listOf("")),
	val chlorine: OxidizerGasConfiguration = OxidizerGasConfiguration(1.5, 500, listOf("")),
	val fluorine: OxidizerGasConfiguration = OxidizerGasConfiguration(2.0, 750, listOf("")),
	val carbonDioxide: GasConfiguration = InertGasConfiguration(1000, listOf("")),
	val helium: GasConfiguration = InertGasConfiguration(500, listOf("")),
) {
	@Serializable
	data class InertGasConfiguration(
		override val maxStored: Int,
		override val factors: List<String>
	) : GasConfiguration

	@Serializable
	data class FuelGasConfiguration(
		val powerPerUnit: Int,
		val cooldown: Int,
		override val maxStored: Int,
		override val factors: List<String>,
	) : GasConfiguration

	@Serializable
	data class OxidizerGasConfiguration(
		val powerMultiplier: Double,
		override val maxStored: Int,
		override val factors: List<String>,
	) : GasConfiguration

	@Serializable
	sealed interface GasConfiguration {
		val factors: List<String>
		val maxStored: Int
		@Transient
		val formattedFactors : List<CollectionFactor> get() = factors.map { CollectionFactor.valueOf(it) }
	}
}
