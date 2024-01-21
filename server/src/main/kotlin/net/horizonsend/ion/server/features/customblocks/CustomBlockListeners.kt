package net.horizonsend.ion.server.features.customblocks

import net.horizonsend.ion.server.features.customitems.CustomBlockItem
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.horizonsend.ion.server.features.customitems.commands.ConvertCommand
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.inventory.InventoryOpenEvent

class CustomBlockListeners : SLEventListener() {

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    @Suppress("Unused")
    fun onCustomBlockPlace(event: BlockPlaceEvent) {
        val player = event.player

        val hand = event.hand
        val itemStack = player.inventory.getItem(hand).clone()
        val item: CustomBlockItem = itemStack.customItem as? CustomBlockItem ?: return
        val blockData: BlockData = item.getCustomBlock()?.blockData ?: return

        event.block.location.block.setBlockData(blockData, true)
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    @Suppress("Unused")
    fun onCustomBlockBreakEvent(event: BlockBreakEvent) {
        if (event.isCancelled) return
        if (event.player.gameMode == GameMode.CREATIVE) return

        val block = event.block
        if (CustomBlocks.getByBlock(block) == null) return

        // Prevents brown mushrooms from dropping
        if (event.isDropItems) {
            event.isDropItems = false
            block.type = Material.AIR
        }

        /*
        val itemUsed = event.player.inventory.itemInMainHand
        val location = block.location.toCenterLocation()
        if (itemUsed.enchantments.containsKey(Enchantment.SILK_TOUCH)) {
            Tasks.sync {
                // Make sure future custom blocks use the same identifier as its custom block identifier
                val drop = CustomItems.getByIdentifier(customBlock.identifier)?.constructItemStack() ?: ItemStack(Material.AIR)
                block.world.dropItem(location, drop)
            }
        } else {
            Tasks.sync {
                for (drop in customBlock.getDrops(itemUsed)) {
                    block.world.dropItem(location, drop)
                }
            }
        }
         */
    }

    @EventHandler
    @Suppress("Unused")
    fun onInventoryOpen(event: InventoryOpenEvent)
    {
        val inventory = event.inventory
        for (item in inventory)
        {
            val newVersion = ConvertCommand.convertCustomMineral(item)
            if (newVersion != null)
            {
                item.type = newVersion.type
                item.itemMeta = newVersion.itemMeta
                item.amount = newVersion.amount
            }
        }
    }
}