package net.horizonsend.ion.server.features.custom.blocks

import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.ConcurrentHashMap

object CustomBlockListeners : SLEventListener() {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Suppress("Unused")
    fun onCustomBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        val hand = event.hand
        val itemStack = player.inventory.getItem(hand).clone()
        val item: CustomBlockItem = itemStack.customItem as? CustomBlockItem ?: return
        val blockData: BlockData = item.getCustomBlock().blockData

        event.block.location.block.setBlockData(blockData, true)
    }

	val noDropEvents: ConcurrentHashMap.KeySetView<BlockBreakEvent, Boolean> = ConcurrentHashMap.newKeySet()

    @EventHandler(priority = EventPriority.HIGHEST)
    @Suppress("Unused")
    fun onCustomBlockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return
        if (event.player.gameMode == GameMode.CREATIVE) return
		if (noDropEvents.remove(event)) return

        val block = event.block
		val customBlock = block.customBlock ?: return

        // Prevents brown mushrooms from dropping
        if (event.isDropItems) {
            event.isDropItems = false
            block.type = Material.AIR
        }

        val itemUsed = event.player.inventory.itemInMainHand
        val location = block.location.toCenterLocation()

        if (itemUsed.enchantments.containsKey(Enchantment.SILK_TOUCH)) Tasks.sync {
			// Make sure future custom blocks use the same identifier as its custom block identifier
			for (drop in customBlock.drops.getDrops(itemUsed, true)) {
				block.world.dropItem(location, drop)
			}
        } else Tasks.sync {
			for (drop in customBlock.drops.getDrops(itemUsed, false)) {
				block.world.dropItem(location, drop)
			}
        }
    }

	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val clickedBlock = event.clickedBlock ?: return
		val customBlock = clickedBlock.customBlock ?: return

		if (customBlock !is InteractableCustomBlock) return

		when (event.action) {
			Action.LEFT_CLICK_BLOCK -> customBlock.onLeftClick(event, clickedBlock)
			Action.RIGHT_CLICK_BLOCK -> customBlock.onRightClick(event, clickedBlock)
			else -> return
		}
	}
}
