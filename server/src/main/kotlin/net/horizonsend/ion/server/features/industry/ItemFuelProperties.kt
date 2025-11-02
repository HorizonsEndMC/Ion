package net.horizonsend.ion.server.features.industry

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.horizonsend.ion.server.core.registration.keys.FluidTypeKeys
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.miscellaneous.utils.items.ItemType
import org.bukkit.Material
import org.bukkit.inventory.ItemStack
import java.util.Optional
import kotlin.jvm.optionals.getOrNull

enum class ItemFuelProperties(val type: ItemType, val burnDurationMillis: Long, val heatOutputJoulesPerSecond: Double, val pollutionResult: FluidStack) {
	COAL(type = ItemType.SimpleMaterial(Material.COAL), burnDurationMillis = 2000, heatOutputJoulesPerSecond = 4000000.0, pollutionResult = FluidStack(FluidTypeKeys.POLLUTION, 20.0)),

	;

	companion object {
		private fun matches(item: ItemStack): ItemFuelProperties? {
			return entries.firstOrNull { it.type.matches(item) }
		}

		private val cache: LoadingCache<ItemStack, Optional<ItemFuelProperties>> = CacheBuilder.newBuilder().build(CacheLoader.from { itemStack -> Optional.ofNullable(matches(itemStack)) })

		operator fun get(item: ItemStack): ItemFuelProperties? = cache[item].getOrNull()
	}
}
