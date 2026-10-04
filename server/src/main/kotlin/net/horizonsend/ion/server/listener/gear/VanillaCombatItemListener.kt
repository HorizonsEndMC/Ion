package net.horizonsend.ion.server.listener.gear


import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.inventory.ItemStack

object VanillaCombatItemListener : SLEventListener() {

	val bannedItems = listOf(
		Material.DIAMOND_SWORD,
		Material.NETHERITE_SWORD,
		Material.WOODEN_AXE,
		Material.STONE_AXE,
		Material.IRON_AXE,
		Material.DIAMOND_AXE,
		Material.NETHERITE_AXE,
		Material.WOODEN_SPEAR,
		Material.STONE_SPEAR,
		Material.IRON_SPEAR,
		Material.DIAMOND_SPEAR,
		Material.NETHERITE_SPEAR,
	)

	@EventHandler
	fun onAttack(event: EntityDamageByEntityEvent, attackCooldown: Any.(ItemStack, Int) -> Unit) {
		val player = event.damager

		if (player !is Player) {
			return
		}
		if (bannedItems.contains(player.inventory.itemInMainHand.type))
			player.setCooldown(player.inventory.itemInMainHand, 100)
			player.attackCooldown(player.inventory.itemInMainHand, 100)
	}
}
