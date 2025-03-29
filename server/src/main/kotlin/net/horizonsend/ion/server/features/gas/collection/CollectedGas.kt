package net.horizonsend.ion.server.features.gas.collection

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.gas.type.Gas
import org.bukkit.Location

@Serializable
data class CollectedGas(
	@Serializable(with = IonRegistryKey.Companion::class)
	private val gasIdentifier: IonRegistryKey<Gas, out Gas>,
	val factorStack: Factor
) {
	val gas get() = gasIdentifier.getValue()

	/**
	 * Returns the amount collected
	 **/
	fun tryCollect(location: Location): CollectionResult {
		if (ConfigurationFiles.globalGassesConfiguration().sandbox) return CollectionResult(gas, 85)
		return CollectionResult(gas, factorStack.getAmount(location))
	}

	fun canBeFound(location: Location): Boolean {
		if (ConfigurationFiles.globalGassesConfiguration().sandbox) return true
		return factorStack.getAmount(location) > 0
	}

	data class CollectionResult(val gas: Gas, val amount: Int)
}
