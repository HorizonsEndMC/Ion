package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.server.features.custom.items.mods.general.PowerCapacityIncrease
import net.horizonsend.ion.server.features.custom.items.objects.ModdedCustomItem
import org.bukkit.inventory.ItemStack

interface ModdedPowerItem : PowerStoringItem, ModdedCustomItem {
	val basePowerCapacity: Int

	override fun getPowerCapacity(itemStack: ItemStack): Int {
		return basePowerCapacity + getMods(itemStack).filterIsInstance<PowerCapacityIncrease>().sumOf { it.increaseAmount }
	}
}
