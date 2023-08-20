package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.kyori.adventure.text.Component
import org.bukkit.Location
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	val factorSupplier: Supplier<List<CollectionFactor>>
	) {
	val factors get(): List<CollectionFactor> = factorSupplier.get()

    fun tryCollect(location: Location): Boolean {
		if (factors.isEmpty()) return false

        return factors.stream().allMatch { factor: CollectionFactor -> factor.factor(location) }
    }

	fun canBeFound(location: Location): Boolean {
		if (factors.isEmpty()) return false

		return factors.stream().allMatch { factor: CollectionFactor -> factor.canBeFound(location) }
	}
}
