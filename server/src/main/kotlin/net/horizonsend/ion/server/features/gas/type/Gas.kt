package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.gas.collection.Factor
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	private val configurationSupplier: Supplier<Gasses.GasConfiguration>
) {
	val configuration get() = configurationSupplier.get()

    fun tryCollect(location: Location): Boolean {
		getFactors(location.world) ?: return false

		return true
    }

	fun canBeFound(location: Location): Boolean {
		val factors = getFactors(location.world)  ?: return false

		return factors.getAmount(location) > - 1
	}

	private fun getFactors(world: World): Factor? {
		val ion = world.ion

		return ion.configuration.gasConfiguration.gasses.firstOrNull { it.gas == this }?.factorStack
	}
}
