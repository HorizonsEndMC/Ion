package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.attribute.SmeltingResultAttribute
import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.inventory.ItemStack

object FurnaceListener : SLEventListener() {
	// something with custom ores
	// NVM: Use FurnaceRecipe to remove the unstable "replace" implementation here
	// (FurnaceRecipe only uses Material for the source :skull:)
	@EventHandler
	fun onFurnaceSmeltCustomOre(event: FurnaceSmeltEvent) {
		val source: ItemStack = event.source
		val customItem = source.customItem

		// If customItem has the Smeltable interface, get the smeltable customItem result
		if (customItem != null) {
			val smeltingAttribute = customItem.getAttributes(source).filterIsInstance<SmeltingResultAttribute>().firstOrNull()
			if (smeltingAttribute != null) {
				event.result = smeltingAttribute.result.get()
				return
			}
		}

		val furnace = event.block.getState(false) as Furnace
		val directional = furnace.blockData as Directional
		val signBlock = furnace.block.getRelative(directional.facing)

		if (!signBlock.type.isWallSign) {
			return
		}

		val sign = signBlock.getState(false) as Sign
		val multiblock: Multiblock? = null

		if (multiblock != null && !multiblock.name.contains("furnace")) {
			event.isCancelled = true
		}

		val result: ItemStack = event.result
		if (result.type == Material.DEAD_BUSH || result.type == Material.DANDELION || source.customItem != null) {
			event.isCancelled = true
		}
	}
}
