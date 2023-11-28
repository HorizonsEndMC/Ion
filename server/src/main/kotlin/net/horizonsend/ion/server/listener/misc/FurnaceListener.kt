package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.customitems.CustomBlockItem
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.minerals.Smeltable
import net.horizonsend.ion.server.features.multiblock.FurnaceMultiblock
import net.horizonsend.ion.server.features.multiblock.Multiblocks
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.getRelativeIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.inventory.ItemStack

object FurnaceListener : SLEventListener() {
	// thing for generally all furnace multiblocks
	@EventHandler
	fun onFurnaceBurn(event: FurnaceBurnEvent) {
		val state = event.block.getState(false) as Furnace

		val directional = state.blockData as Directional
		val signBlock = state.block.getRelativeIfLoaded(directional.facing) ?: return

		val type = signBlock.type
		if (!type.isWallSign) {
			return
		}

		state.world.monsterSpawnLimit

		val sign = signBlock.getState(false) as Sign
		val checkStructure = false
		val loadChunks = false
		val multiblock = Multiblocks[sign, checkStructure, loadChunks]

		if (multiblock is FurnaceMultiblock) {
			if (Multiblocks[sign, true, false] !== multiblock) {
				event.isCancelled = true
				return
			}

			multiblock.onFurnaceTick(event, state, sign)
		}
	}

	// something with custom ores
	// TODO: Use FurnaceRecipe to remove the unstable "replace" implementation here
	@EventHandler
	fun onFurnaceSmeltCustomOre(event: FurnaceSmeltEvent) {
		val source: ItemStack = event.source
		val item = source.customItem

		// Disable legacy custom item smelting
		if (net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems[source] is
					net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomBlockItem &&
			item == null
			) {
			event.result = when (source.itemMeta.customModelData) {
				1 -> CustomItems.ALUMINUM_INGOT.constructItemStack()
				2 -> CustomItems.CHETHERITE.constructItemStack()
				3 -> CustomItems.TITANIUM_INGOT.constructItemStack()
				4 -> CustomItems.URANIUM.constructItemStack()
				else -> ItemStack(Material.AIR)
			}
			return
		}

		// If customItem has the Smeltable interface, get the smeltable customItem result
		if (item is CustomBlockItem && item is Smeltable) {
			event.result = CustomItems.getByIdentifier(item.smeltResultIdentifier)?.constructItemStack() ?: return
			return
		}

		val furnace = event.block.getState(false) as Furnace
		val directional = furnace.blockData as Directional
		val signBlock = furnace.block.getRelative(directional.facing)

		if (!signBlock.type.isWallSign) {
			return
		}

		val sign = signBlock.getState(false) as Sign
		val multiblock = Multiblocks[sign, false]

		if (multiblock != null && !multiblock.name.contains("furnace")) {
			event.isCancelled = true
		}

		val result: ItemStack = event.result
		if (result.type == Material.DEAD_BUSH || result.type == Material.DANDELION || source.customItem != null) {
			event.isCancelled = true
		}
	}
}
