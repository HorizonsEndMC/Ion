package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor

@Serializable
data class GasConfiguration(
	val hydrogen: Fuel = Fuel(2, 150, listOf("")),
	val nitrogen: Fuel = Fuel(1, 100, listOf("")),
	val methane: Fuel = Fuel(3, 200, listOf("")),
	val oxygen: Oxidizer = Oxidizer(1.0, listOf("")),
	val chlorine: Oxidizer = Oxidizer(1.5, listOf("")),
	val fluorine: Oxidizer = Oxidizer(2.0, listOf("")),
	val carbonDioxide: Gas = InertGas(listOf("")),
	val helium: Gas = InertGas(listOf("")),
) {
	@Serializable
	data class InertGas(
		override val factors: List<String>
	) : Gas

	@Serializable
	data class Fuel(
		val powerPerUnit: Int,
		val cooldown: Int,
		override val factors: List<String>,
	) : Gas

	@Serializable
	data class Oxidizer(
		val powerMultiplier: Double,
		override val factors: List<String>,
	) : Gas

	@Serializable
	sealed interface Gas {
		val factors: List<String>

		@Transient
		val formattedFactors : List<CollectionFactor> get() = factors.map { CollectionFactor.valueOf(it) }
	}
}
