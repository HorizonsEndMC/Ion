package net.horizonsend.ion.server.features.gas.type

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import net.horizonsend.ion.server.features.gas.collection.CollectedGas
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor

@Serializable
data class WorldGasConfiguration(
	val gasses: List<CollectedGas> = listOf(),

	val hydrogen: CollectionFactors = CollectionFactors(),
	val nitrogen: CollectionFactors = CollectionFactors(),
	val methane: CollectionFactors = CollectionFactors(),
	val oxygen: CollectionFactors = CollectionFactors(),
	val chlorine: CollectionFactors = CollectionFactors(),
	val fluorine: CollectionFactors = CollectionFactors(),
	val carbonDioxide: CollectionFactors = CollectionFactors(),
	val helium: CollectionFactors = CollectionFactors(),
) {
	@Serializable
	data class CollectionFactors(
		val factors: List<String> = listOf()
	) {
		@Transient
		val formattedFactors : List<CollectionFactor> get() = factors.map { CollectionFactor.valueOf(it) }
	}
}
