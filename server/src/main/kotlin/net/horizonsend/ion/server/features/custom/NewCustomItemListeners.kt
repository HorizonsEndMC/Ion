package net.horizonsend.ion.server.features.custom

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.newCustomItem
import net.horizonsend.ion.server.features.custom.items.components.ListenerComponent
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.Material
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.meta.Damageable

object NewCustomItemListeners : SLEventListener() {
	// Presorted to avoid a bunch of filtering for every event at runtime
	private val interactListeners: MutableMap<NewCustomItem, MutableSet<ListenerComponent<PlayerInteractEvent, *>>> = mutableMapOf()
	private val swapItemListeners: MutableMap<NewCustomItem, MutableSet<ListenerComponent<PlayerSwapHandItemsEvent, *>>> = mutableMapOf()
	private val dispenseListeners: MutableMap<NewCustomItem, MutableSet<ListenerComponent<BlockPreDispenseEvent, *>>> = mutableMapOf()
	private val entityShootBowListeners: MutableMap<NewCustomItem, MutableSet<ListenerComponent<EntityShootBowEvent, *>>> = mutableMapOf()

	private fun <E: Event, T: NewCustomItem> getListeners(
		collection: MutableMap<NewCustomItem, MutableSet<ListenerComponent<E, *>>>,
		item: T
	): MutableSet<ListenerComponent<E, *>> {
		return collection.getOrPut(item) { mutableSetOf() }
	}

	fun sortCustomItemListeners() {
		for (newCustomItem in CustomItemRegistry.ALL) {
			newCustomItem.customComponents.filterIsInstanceTo<ListenerComponent<PlayerInteractEvent, *>, MutableSet<ListenerComponent<PlayerInteractEvent, *>>>(getListeners(interactListeners, newCustomItem))
			newCustomItem.customComponents.filterIsInstanceTo<ListenerComponent<PlayerSwapHandItemsEvent, *>, MutableSet<ListenerComponent<PlayerSwapHandItemsEvent, *>>>(getListeners(swapItemListeners, newCustomItem))
			newCustomItem.customComponents.filterIsInstanceTo<ListenerComponent<BlockPreDispenseEvent, *>, MutableSet<ListenerComponent<BlockPreDispenseEvent, *>>>(getListeners(dispenseListeners, newCustomItem))
			newCustomItem.customComponents.filterIsInstanceTo<ListenerComponent<EntityShootBowEvent, *>, MutableSet<ListenerComponent<EntityShootBowEvent, *>>>(getListeners(entityShootBowListeners, newCustomItem))
		}
	}

	// For durability manipulation
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerItemDamageEvent(event: PlayerItemDamageEvent) {
		if (event.item.newCustomItem == null) return
		if (event.item.itemMeta !is Damageable) return
		event.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun playerInteractListener(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		val item = event.item ?: return
		val customItem = item.newCustomItem ?: return

		val listeners = getListeners(interactListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, item) }
		}
	}

	@EventHandler
	fun onEntityShootBow(event: EntityShootBowEvent) {
		val offhand = event.entity.equipment?.itemInMainHand ?: return
		val customItem = offhand.newCustomItem ?: return

		val listeners = getListeners(entityShootBowListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, offhand) }
		}
	}

	@EventHandler
	fun onPlayerSwapItem(event: PlayerSwapHandItemsEvent) {
		// We have to get it from the inventory and not the event, otherwise things break
		val itemStack = event.player.inventory.itemInMainHand
		val customItem = itemStack.newCustomItem ?: return

		val listeners = getListeners(swapItemListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, itemStack) }
		}
	}

	@EventHandler
	fun onItemDispensed(event: BlockPreDispenseEvent) {
		// Retain the dispenser/ dropper parity
		if (event.block.type != Material.DISPENSER) return
		val customItem = event.itemStack.newCustomItem ?: return

		val listeners = getListeners(dispenseListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, event.itemStack) }
		}
	}
}
