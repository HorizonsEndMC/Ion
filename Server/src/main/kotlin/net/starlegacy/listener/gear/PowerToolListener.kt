package net.starlegacy.listener.gear

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.starlegacy.feature.gear.TreeCutter
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.misc.getPower
import net.starlegacy.feature.misc.removePower
import net.starlegacy.listener.SLEventListener
import org.bukkit.Bukkit
import org.bukkit.ChatColor
import org.bukkit.Effect
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.Action
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object PowerToolListener : SLEventListener() {
	private val PICKAXE = ItemStack(Material.DIAMOND_PICKAXE, 1)

	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onInteract(event: PlayerInteractEvent) {
		if (event.action != Action.LEFT_CLICK_BLOCK || event.player.gameMode == GameMode.CREATIVE) {
			return
		}

		val item = event.item ?: return
		val customItem = CustomItems[item]
		if (customItem == null || !customItem.id.startsWith("power_tool_")) {
			return
		}
		val type = customItem.id.split("_")[2]
		val player = event.player
		val block = event.clickedBlock ?: return
		val blockType = block.type
		when (type) {
			"drill" -> {
				if (blockType == Material.BEDROCK || blockType == Material.BARRIER) {
					return
				}

				if (!BlockBreakEvent(block, player).callEvent()) {
					return
				}

				if (blockType != block.type) return

				if (getPower(item) < 20) {
					player.sendMessage(ChatColor.RED.toString() + "Out of power.")
					return
				}

				removePower(item, 10)
				player.world.playSound(player.location, Sound.ENTITY_ZOMBIE_ATTACK_IRON_DOOR, 0.1f, 1.5f)
				block.world.playEffect(block.location, Effect.STEP_SOUND, blockType)
				block.breakNaturally(PICKAXE)
				if (blockType == Material.END_PORTAL_FRAME) {
					player.world.dropItem(block.location, ItemStack(Material.END_PORTAL_FRAME))
				}

				return
			}

			"chainsaw" -> {
				val breakEvent = BlockBreakEvent(block, player)
				Bukkit.getPluginManager().callEvent(breakEvent)
				if (breakEvent.isCancelled) return

				event.isCancelled = true

				if (!TreeCutter.isApplicable(blockType)) {
					return
				}

				if (getPower(item) < 1000) {
					player.sendMessage(ChatColor.RED.toString() + "Out of power.")
					return
				}

				removePower(item, 1000)

				TreeCutter(event.player, block).runTaskAsynchronously(Ion)
				return
			}

			else -> println("Unhandled power tool $type")
		}
	}
}