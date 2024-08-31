package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.gas.collection.Factor
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import org.bukkit.World
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	private val configurationSupplier: Supplier<Gasses.GasConfiguration>
) {
	val configuration get() = configurationSupplier.get()

	private fun getFactors(world: World): Factor? {
		val ion = world.ion

		return ion.configuration.gasConfiguration.gasses.firstOrNull { it.gas == this }?.factorStack
	}
}
