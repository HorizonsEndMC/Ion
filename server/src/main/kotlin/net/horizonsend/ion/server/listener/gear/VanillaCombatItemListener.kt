package net.horizonsend.ion.server.listener.gear


import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.EntityDamageByEntityEvent

object VanillaCombatItemListener : SLEventListener() {

	val bannedItems = listOf(
		Material.NETHERITE_SWORD,
		Material.COPPER_AXE,
		Material.IRON_AXE,
		Material.DIAMOND_AXE,
		Material.NETHERITE_AXE,
		Material.WOODEN_SPEAR,
		Material.STONE_SPEAR,
		Material.COPPER_SPEAR,
		Material.IRON_SPEAR,
		Material.DIAMOND_SPEAR,
		Material.NETHERITE_SPEAR,
	)
	@EventHandler
	fun onAttack(event: EntityDamageByEntityEvent){
		val player = event.damager
		val victim = event.entity
		if (player !is Player) {
			return
		}
		if (bannedItems.contains(player.inventory.itemInMainHand.type))
			if (victim is Player) {
				event.isCancelled = true
	}}
}
