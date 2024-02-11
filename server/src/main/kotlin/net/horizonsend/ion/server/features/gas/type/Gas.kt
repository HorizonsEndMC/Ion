package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.gas.collectionfactors.CollectionFactor
import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	val configurationSupplier: Supplier<Gasses.GasConfiguration>,
	val collectionFactorSupplier: (IonWorld) -> List<CollectionFactor>
) {
	val configuration = configurationSupplier.get()

	fun getFactors(world: World): List<CollectionFactor> = collectionFactorSupplier(world.ion)

    fun tryCollect(location: Location): Boolean {
		val factors = getFactors(location.world)

		if (factors.isEmpty()) return false

        return factors.stream().allMatch { factor: CollectionFactor ->
			return@allMatch factor.factor(location)
		}
    }

	fun canBeFound(location: Location): Boolean {
		val factors = getFactors(location.world)

		if (factors.isEmpty()) return false

		return factors.all { factor: CollectionFactor -> factor.canBeFound(location) }
	}
}
