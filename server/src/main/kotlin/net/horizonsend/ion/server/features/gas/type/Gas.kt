package net.horizonsend.ion.server.features.gas.type

import net.horizonsend.ion.server.configuration.Gasses
import net.horizonsend.ion.server.features.gas.collection.Factor
import net.horizonsend.ion.server.features.transport.fluids.types.GasPipedFluid
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.kyori.adventure.text.Component
import org.bukkit.World
import java.util.function.Supplier

abstract class Gas(
	val identifier: String,
	val displayName: Component,
	val containerIdentifier: String,

	private val configurationSupplier: Supplier<Gasses.GasConfiguration>,
	private val transportedFluidSupplier: Supplier<GasPipedFluid>
) {
	val configuration get() = configurationSupplier.get()
	val fluid get() = transportedFluidSupplier.get()

	private fun getFactors(world: World): Factor? {
		val ion = world.ion

		return ion.configuration.gasConfiguration.gasses.firstOrNull { it.gas == this }?.factorStack
	}
}
