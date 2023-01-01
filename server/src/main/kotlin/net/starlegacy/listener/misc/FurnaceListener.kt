package net.starlegacy.listener.misc

import net.starlegacy.feature.misc.CustomBlockItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.multiblock.FurnaceMultiblock
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.getRelativeIfLoaded
import net.starlegacy.util.isWallSign
import net.starlegacy.util.time
import net.starlegacy.util.timing
import org.bukkit.Material
import org.bukkit.block.Furnace
import org.bukkit.block.Sign
import org.bukkit.block.data.Directional
import org.bukkit.event.EventHandler
import org.bukkit.event.inventory.FurnaceBurnEvent
import org.bukkit.event.inventory.FurnaceSmeltEvent
import org.bukkit.inventory.ItemStack

object FurnaceListener : SLEventListener() {
	// lazy to ensure loaded after multiblocks
	val timings by lazy {
		Multiblocks.all().filterIsInstance<FurnaceMultiblock>()
			.associateWith { timing("furnace-multiblock-${it.javaClass.simpleName}") }
	}

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
			timings.getValue(multiblock).time {
				multiblock.onFurnaceTick(event, state, sign)
			}
		}
	}

	// something with custom ores
	@EventHandler
	fun onFurnaceSmeltCustomOre(event: FurnaceSmeltEvent) {
		val source: ItemStack = event.source
		val item = CustomItems[source]

		if (item is CustomBlockItem && item.id.endsWith("_ore")) {
			event.result = CustomItems[item.id.replace("_ore", "")]!!.itemStack(1)
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
		if (result.type == Material.DEAD_BUSH || result.type == Material.DANDELION || CustomItems[source] != null) {
			event.isCancelled = true
		}
	}
}
