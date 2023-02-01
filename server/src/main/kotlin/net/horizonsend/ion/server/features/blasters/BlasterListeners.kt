package net.horizonsend.ion.server.features.blasters

import net.horizonsend.ion.server.features.blasters.objects.Blaster
import net.horizonsend.ion.server.features.blasters.objects.Magazine
import net.horizonsend.ion.server.features.customItems.CustomItems
import net.horizonsend.ion.server.features.customItems.CustomItems.customItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.minimessage.MiniMessage
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Nation
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.entity.PlayerDeathEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerItemHeldEvent
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType
import kotlin.math.roundToInt

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
			"<#" + Integer.toHexString(
				(
					SLPlayer[victim.uniqueId]?.nation?.let { Nation.findById(it) }?.color
						?: 16777215
					)
			) + ">"

		val killerColor =
			"<#" + Integer.toHexString(
				(
					SLPlayer[killer.uniqueId]?.nation?.let { Nation.findById(it) }?.color
						?: 16777215
					)
			) + ">"

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

	@EventHandler
	fun onPrepareItemCraftEvent(event: PrepareItemCraftEvent) {
		if (!event.isRepair) return // Will always be a combination of 2 items.

		val magazines = event.inventory.matrix.filter { it?.customItem is Magazine<*> }.filterNotNull()

		if (magazines.isEmpty()) return
		if (magazines.first().customItem?.identifier != magazines.last().customItem?.identifier) return

		val magazineType = magazines.first().customItem as Magazine<*>
		val totalAmmo = magazines.sumOf { magazineType.getAmmunition(it) }.coerceIn(0..magazineType.balancing.capacity)
		val resultItemStack = CustomItems.getByIdentifier(magazineType.identifier)!!.constructItemStack()

		magazineType.setAmmunition(resultItemStack, event.inventory, totalAmmo)

		event.inventory.result = resultItemStack
	}
}
