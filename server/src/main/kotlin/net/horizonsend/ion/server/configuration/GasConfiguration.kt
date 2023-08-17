package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor

@Serializable
data class GasConfiguration(
	val hydrogen: Gas = Gas(listOf()),
	val nitrogen: Gas = Gas(listOf()),
	val methane: Gas = Gas(listOf()),
	val oxygen: Gas = Gas(listOf()),
	val chlorine: Gas = Gas(listOf()),
	val fluorine: Gas = Gas(listOf()),
	val carbonDioxide: Gas = Gas(listOf()),
	val helium: Gas = Gas(listOf())
) {
	@Serializable
	data class Gas(
		val factors: List<String>
	) {
		@Transient
		val formattedFactors : List<CollectionFactor> = factors.map { CollectionFactor.valueOf(it) }
	}
}
