package net.starlegacy.listener.misc

import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.multiblock.misc.MobDefender
import net.starlegacy.listener.SLEventListener
import net.starlegacy.util.filtered
import net.starlegacy.util.stripColor
import net.starlegacy.util.subscribe
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPhysicsEvent
import org.bukkit.event.block.SignChangeEvent
import org.bukkit.event.world.StructureGrowEvent

object BlockListener : SLEventListener() {
    override fun onRegister() {
        // Disable block physics for portals to prevent airlocks from breaking
        subscribe<BlockPhysicsEvent>()
            .filtered { it.block.type == Material.END_PORTAL }
            .handler { event -> event.isCancelled = true }

        // Don't allow breaking blocks with custom items
        subscribe<BlockBreakEvent>().handler { event ->
            val item = event.player.inventory.itemInMainHand
            val customItem = CustomItems[item]
            if (customItem != null && !customItem.id.startsWith("power_tool_")) {
                event.isCancelled = true
            }
        }

        // Prevent huge mushroom trees from growing as large mushroom blocks are used as custom ores
        subscribe<StructureGrowEvent>()
            .filtered { it.blocks.any { it.type == Material.BROWN_MUSHROOM_BLOCK } }
            .handler { event -> event.isCancelled = true }

        // Attempt to remove mob defenders at the location of blocks broken
        subscribe<BlockBreakEvent>(EventPriority.MONITOR)
            .filtered { !it.isCancelled }
            .handler { event -> MobDefender.removeDefender(event.block.location) }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    fun onSignChange(event: SignChangeEvent) {
        for (i in 0 until 4) {
            event.setLine(i, event.getLine(i)?.stripColor())
        }
    }
}
