package net.horizonsend.ion.server.miscellaneous.listeners

import net.horizonsend.ion.server.extensions.userError
import net.starlegacy.feature.starship.DeactivatedPlayerStarships
import net.starlegacy.feature.starship.PlayerStarshipState
import net.starlegacy.feature.starship.event.StarshipPilotEvent
import net.starlegacy.util.blockKey
import net.starlegacy.util.blockKeyX
import net.starlegacy.util.blockKeyY
import net.starlegacy.util.blockKeyZ
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.block.data.BlockData
import org.bukkit.enchantments.Enchantment
import org.bukkit.enchantments.EnchantmentOffer
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.enchantment.EnchantItemEvent
import org.bukkit.event.enchantment.PrepareItemEnchantEvent
import org.bukkit.inventory.ItemStack

class GameplayTweaksListeners : Listener {
	@EventHandler
	@Suppress("Unused")
	fun craftPilotEvent(event: StarshipPilotEvent) {
		val state: PlayerStarshipState = DeactivatedPlayerStarships.getSavedState(event.data) ?: return

		for ((blockKey: Long, block: BlockData) in state.blockMap) {
			val vector = Location(
				event.player.world, blockKeyX(blockKey).toDouble(),
				blockKeyY(blockKey).toDouble(), blockKeyZ(blockKey).toDouble()
			)

			val above = vector.add(
				0.0,
				1.0,
				0.0
			)

			if (block.material.name.contains("PISTON") &&
				above.block.type.name.contains("SHULKER") &&
				!state.blockMap.contains(blockKey(above.x, above.y, above.z))
			) {
				event.isCancelled = true
				event.player.userError("Undetected crates found: redetect the craft before piloting!")
			}
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onEnchantItemEvent(event: EnchantItemEvent) {
		event.isCancelled = true

		event.enchanter.level -= 120
		event.inventory.getItem(1)?.amount = event.inventory.getItem(1)?.amount!! - 1

		if (event.item.type == Material.BOOK) {
			event.inventory.removeItem(event.item)

			val book = ItemStack(Material.ENCHANTED_BOOK)
			book.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)

			event.inventory.addItem(book)
		} else {
			event.item.addUnsafeEnchantment(Enchantment.SILK_TOUCH, 1)
		}
	}

	@EventHandler
	@Suppress("Unused")
	fun onPrepareItemEnchantEvent(event: PrepareItemEnchantEvent) {
		event.offers[0] =
			if (Enchantment.SILK_TOUCH.canEnchantItem(event.item) || event.item.type == Material.BOOK) {
				EnchantmentOffer(Enchantment.SILK_TOUCH, 1, 120)
			} else {
				null
			}
		event.offers[1] = null
		event.offers[2] = null
	}
}
