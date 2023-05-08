package net.horizonsend.ion.server.features.blasters

import kotlin.math.roundToInt
import net.horizonsend.ion.common.database.Nation
import net.horizonsend.ion.server.features.blasters.objects.Blaster
import net.horizonsend.ion.server.features.blasters.objects.Magazine
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.cache.nations.PlayerCache
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.Listener
import org.bukkit.event.block.Action
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import org.jetbrains.exposed.sql.transactions.transaction

class BlasterListeners : Listener {
	@Suppress("Unused")
	@EventHandler
	fun onDeath(event: PlayerDeathEvent) {
		val victim = event.player
		val killer = event.entity.killer ?: return
		val customItem = killer.inventory.itemInMainHand.customItem ?: return

		if (customItem !is Blaster<*>) return

		val blaster = customItem.displayName
		val victimColor =
			"<#" + Integer.toHexString((
					PlayerCache[victim].nationOid?.let { transaction { Nation[it]?.color } }
						?: 16777215
					)) + ">"

		val killerColor =
			"<#" + Integer.toHexString((
					PlayerCache[killer].nationOid?.let { transaction { Nation[it]?.color } }
						?: 16777215
					)) + ">"

		val distance = killer.location.distance(victim.location)
		val verb = when (customItem.identifier) {
			"SNIPER" -> "sniped"
			"SHOTGUN" -> "blasted"
			"RIFLE" -> "shot"
			"SUBMACHINE_BLASTER" -> "shredded"
			"PISTOL" -> "pelted"
			else -> "shot"
		}

		val newMessage = MiniMessage.miniMessage()
			.deserialize(
				"$victimColor${victim.name}<reset> was $verb by $killerColor${killer.name}<reset> from ${distance.roundToInt()} blocks away, using "
			)
			.append(blaster)

		event.deathMessage(newMessage)
	}

	@EventHandler
	fun onPlayerItemHoldEvent(event: PlayerItemHeldEvent) {
		val itemStack = event.player.inventory.getItem(event.newSlot) ?: return
		val customItem = itemStack.customItem as? Blaster<*> ?: return

		// adding a potion effect because it takes ages for that attack cooldown to come up
		event.player.addPotionEffect(PotionEffect(PotionEffectType.FAST_DIGGING, 20, 5, false, false, false))

		val ammunition = customItem.getAmmunition(itemStack)

		event.player.sendActionBar(
			Component.text(
				"Ammo: $ammunition / ${customItem.balancing.magazineSize}",
				NamedTextColor.RED
			)
		)
	}

	@EventHandler(priority = EventPriority.LOWEST)
	@Suppress("Unused")
	fun onLeftClick(event: PlayerInteractEvent) {
		if (event.item == null) return

		val customItem = event.item?.customItem ?: return
		when (event.action) {
			Action.LEFT_CLICK_AIR, Action.LEFT_CLICK_BLOCK -> {
				if (customItem is Blaster<*>) event.isCancelled = true
			}

			else -> return // Unknown Action Enum - We probably don't care, silently fail
		}
	}

	@EventHandler
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (!event.isRepair) return // Will always be a combination of 2 items.

		val craftedItems = event.inventory.matrix.filter { it?.customItem is Magazine<*> }.filterNotNull()

		// Only magazines of the same type accepted
		if (craftedItems.isEmpty() ||
			craftedItems.first().customItem?.identifier != craftedItems.last().customItem?.identifier) {
			event.inventory.result = null
			return
		}

		val resultItem = craftedItems.first().customItem as Magazine<*>
		val totalAmmo = craftedItems.sumOf { resultItem.getAmmunition(it) }.coerceIn(0..resultItem.balancing.capacity)
		val resultItemStack = CustomItems.getByIdentifier(resultItem.identifier)!!.constructItemStack()
		resultItem.setAmmunition(resultItemStack, event.inventory, totalAmmo)

		event.inventory.result = resultItemStack
	}
}
