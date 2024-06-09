package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.server.features.custom.items.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.mods.tool.PowerUsageIncrease
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import org.bukkit.inventory.ItemStack
import kotlin.math.roundToInt

/**
 * A combined interface which handles some of the integration of mods and power
 **/
interface ModdedPowerItem : PoweredItem, ModdedCustomItem {
	val basePowerCapacity: Int
	val basePowerUsage: Int

	override fun getPowerCapacity(itemStack: ItemStack): Int {
		return basePowerCapacity + getMods(itemStack).filterIsInstance<PowerCapacityIncrease>().sumOf { it.increaseAmount }
	}

	override fun getPowerUse(itemStack: ItemStack): Int {
		var usage = basePowerUsage.toDouble()

		for (increase in getMods(itemStack).filterIsInstance<PowerUsageIncrease>()) {
			usage *= increase.usageMultiplier
		}

		return usage.roundToInt()
	}
}
