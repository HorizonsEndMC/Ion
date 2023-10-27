package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses.GasConfiguration
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	val configurationSupplier: Supplier<GasConfiguration>
	) {
	val configuration = configurationSupplier.get()
	val factors: List<CollectionFactor> = configurationSupplier.get().formattedFactors

    fun tryCollect(location: Location): Boolean {
		if (factors.isEmpty()) return false

        return factors.stream().allMatch { factor: CollectionFactor ->
			return@allMatch factor.factor(location)
		}
    }

	fun canBeFound(location: Location): Boolean {
		if (factors.isEmpty()) return false

		return factors.all { factor: CollectionFactor -> factor.canBeFound(location) }
	}
}
