package net.horizonsend.ion.server.features.transport.fluids.types.steam

import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory
import net.horizonsend.ion.server.features.transport.fluids.types.GasFluid
import net.kyori.adventure.text.Component
import org.bukkit.Color

object UltraDenseSteam : GasFluid(
	key = FluidTypeKeys.ULTRA_DENSE_STEAM,
	color = Color.WHITE,
	heatCapacity = 2.030,
	molarMass = 18.01528,
	pressureBars = 45.0
) {
	override val categories: Array<FluidCategory> = arrayOf(FluidCategory.GAS, FluidCategory.STEAM)

	override fun getDisplayName(stack: FluidStack): Component {
		return Component.text("Ultra Dense Steam")
	}
}
