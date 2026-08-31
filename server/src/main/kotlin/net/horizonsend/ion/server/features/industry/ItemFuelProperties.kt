package net.horizonsend.ion.server.features.industry

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import org.bukkit.Material
import org.bukkit.inventory.ItemStack

private fun customItem(key: IonRegistryKey<CustomItem, out CustomItem>): ItemStack = key.getValue().constructItemStack()
private fun vanillaItem(material: Material): ItemStack = ItemStack(material, 1)

enum class ItemFuelProperties(
	private val item: ItemStack,
	val burnDurationMillis: Long,
	val heatOutputJoulesPerSecond: Double,
	val pollutionResult: FluidStack
) {
	COAL(
		item = vanillaItem(Material.COAL),
		burnDurationMillis = 2_000,
		heatOutputJoulesPerSecond = 2_500_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	COAL_BLOCK(
		item = vanillaItem(Material.COAL_BLOCK),
		burnDurationMillis = 18_000,
		heatOutputJoulesPerSecond = 2_800_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	URANIUM(
		item = customItem(CustomItemKeys.URANIUM),
		burnDurationMillis = 100_000,
		heatOutputJoulesPerSecond = 2_300_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	URANIUM_BLOCK(
		item = customItem(CustomItemKeys.URANIUM_BLOCK),
		burnDurationMillis = 900_000,
		heatOutputJoulesPerSecond = 2_400_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	REDSTONE(
		item = vanillaItem(Material.REDSTONE),
		burnDurationMillis = 3_750,
		heatOutputJoulesPerSecond = 100_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	REDSTONE_BLOCK(
		item = vanillaItem(Material.REDSTONE_BLOCK),
		burnDurationMillis = 33_750,
		heatOutputJoulesPerSecond = 120_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	DRIED_KELP_BLOCK(
		item = vanillaItem(Material.DRIED_KELP_BLOCK),
		burnDurationMillis = 250 * 9,
		heatOutputJoulesPerSecond = 40_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	),
	DRIED_KELP(
		item = vanillaItem(Material.DRIED_KELP),
		burnDurationMillis = 250,
		heatOutputJoulesPerSecond = 40_000.0,
		pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)
	);

	companion object {
		private val itemMap: Map<String, ItemFuelProperties> = entries.associateBy { fuel -> createKey(fuel.item) }

		operator fun get(item: ItemStack): ItemFuelProperties? = itemMap[createKey(item)]

		private fun createKey(item: ItemStack): String = item.customItem?.identifier ?: item.type.name
	}
}
