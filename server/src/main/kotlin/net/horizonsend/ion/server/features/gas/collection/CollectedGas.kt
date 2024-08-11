package net.horizonsend.ion.server.features.gas.collection

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.gas.Gasses
import net.horizonsend.ion.server.features.gas.type.Gas
import org.bukkit.Location

@Serializable
data class CollectedGas(
	private val gasIdentifier: String,
	val factors: Factor
) {
	val gas get() = Gasses[gasIdentifier]

	/**
	 * Returns the amount collected
	 **/
	private fun tryCollect(location: Location): CollectionResult {
		return CollectionResult(gas, factors.getAmount(location))
	}

	data class CollectionResult(val gas: Gas, val amount: Int)
}
