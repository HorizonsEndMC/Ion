package net.horizonsend.ion.server.features.industry

import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

enum class ItemFuelProperties(
	val material: Material,
	val burnDurationMillis: Long,
	val heatOutputJoulesPerSecond: Double,
	val pollutionResult: FluidStack
) {
	COAL(
		material = Material.COAL,
		burnDurationMillis = 2_000,
		heatOutputJoulesPerSecond = 4_000_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	COAL_BLOCK(
		material = Material.COAL_BLOCK,
		burnDurationMillis = 18_000,
		heatOutputJoulesPerSecond = 3_500_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	DRIED_KELP_BLOCK(
		material = Material.DRIED_KELP_BLOCK,
		burnDurationMillis = 250 * 9,
		heatOutputJoulesPerSecond = 40_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	DRIED_KELP(
		material = Material.DRIED_KELP,
		burnDurationMillis = 250,
		heatOutputJoulesPerSecond = 40_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	);

	companion object {
		operator fun get(item: ItemStack): ItemFuelProperties? = entries.firstOrNull { fuel -> fuel.material == item.type }
	}
}
