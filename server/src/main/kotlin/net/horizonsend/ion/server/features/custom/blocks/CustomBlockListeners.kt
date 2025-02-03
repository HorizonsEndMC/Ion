package net.horizonsend.ion.server.features.custom.blocks

import io.papermc.paper.event.player.PlayerPickItemEvent
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks.customBlock
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.type.CustomBlockItem
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.server.network.ServerGamePacketListenerImpl
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.attribute.Attribute
import org.bukkit.enchantments.Enchantment
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.player.PlayerInteractEvent
import java.util.concurrent.ConcurrentHashMap
import kotlin.math.roundToInt

object CustomBlockListeners : SLEventListener() {
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Suppress("Unused")
    fun onCustomBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        val hand = event.hand
        val itemStack = player.inventory.getItem(hand).clone()
        val item: CustomBlockItem = itemStack.customItem as? CustomBlockItem ?: return
        val block = item.getCustomBlock()

        event.block.location.block.setBlockData(block.blockData, true)
		block.placeCallback(itemStack, event.block)
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

	// Avoid magic string
	private val entityPickMethodName = ServerGamePacketListenerImpl::handlePickItemFromEntity.name

	@EventHandler
	fun onPlayerPickBlock(event: PlayerPickItemEvent) {
		if (Thread.currentThread().stackTrace.any { element -> element.methodName == entityPickMethodName }) return
		val player = event.player

		val targetedBlock = player.getTargetBlockExact(player.getAttribute(Attribute.BLOCK_INTERACTION_RANGE)?.value?.roundToInt() ?: 5) ?: return
		val customBlock = CustomBlocks.getByBlockData(targetedBlock.blockData) ?: return
		val customBlockItem = customBlock.customItem.constructItemStack()

		// Source slot is for survival, when taking the item from the slot in the inventory, -1 if it is not present, e.g. creative mode
		// Target slot is the slot in the hotbar that the picked block will go to

		if (player.gameMode == GameMode.CREATIVE) {
			if (event.player.inventory.containsAtLeast(customBlockItem, 1)) {
				val index = event.player.inventory.indexOfFirst { stack -> stack?.isSimilar(customBlockItem) == true }

				// If in hotbar, jump to slot, else grab from inventory
				if (index in 0..8) {
					event.targetSlot = index
					event.sourceSlot = index
				} else {
					event.sourceSlot = index
				}
			} else {
				event.isCancelled = true
				event.player.inventory.setItem(event.targetSlot, customBlockItem)
			}
		}

		if (player.gameMode == GameMode.SURVIVAL) {
			if (event.player.inventory.containsAtLeast(customBlockItem, 1)) {
				val index = event.player.inventory.indexOfFirst { stack -> stack?.isSimilar(customBlockItem) == true }

				// If in hotbar, jump to slot, else grab from inventory
				if (index in 0..8) {
					event.targetSlot = index
					event.sourceSlot = index
				} else {
					event.sourceSlot = index
				}
			}
		}
	}
}
