package net.horizonsend.ion.server.features.custom

import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.components.Listener
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
	private val interactListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerInteractEvent, *>>> = mutableMapOf()
	private val swapItemListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerSwapHandItemsEvent, *>>> = mutableMapOf()
	private val dispenseListeners: MutableMap<CustomItem, MutableSet<Listener<BlockPreDispenseEvent, *>>> = mutableMapOf()
	private val entityShootBowListeners: MutableMap<CustomItem, MutableSet<Listener<EntityShootBowEvent, *>>> = mutableMapOf()

	private fun <E: Event, T: CustomItem> getListeners(
        collection: MutableMap<CustomItem, MutableSet<Listener<E, *>>>,
        item: T
	): MutableSet<Listener<E, *>> {
		return collection.getOrPut(item) { mutableSetOf() }
	}

	fun sortCustomItemListeners() {
		for (newCustomItem in CustomItemRegistry.ALL) {
			val components = newCustomItem.allComponents()

			components.filterIsInstance<Listener<PlayerInteractEvent, *>>().filterTo(getListeners(interactListeners, newCustomItem)) { it.eventType == PlayerInteractEvent::class }
			components.filterIsInstance<Listener<PlayerSwapHandItemsEvent, *>>().filterTo(getListeners(swapItemListeners, newCustomItem)) { it.eventType == PlayerSwapHandItemsEvent::class }
			components.filterIsInstance<Listener<BlockPreDispenseEvent, *>>().filterTo(getListeners(dispenseListeners, newCustomItem)) { it.eventType == BlockPreDispenseEvent::class }
			components.filterIsInstance<Listener<EntityShootBowEvent, *>>().filterTo(getListeners(entityShootBowListeners, newCustomItem)) { it.eventType == EntityShootBowEvent::class }
		}
	}

	// For durability manipulation
	@EventHandler(priority = EventPriority.LOWEST)
	fun onPlayerItemDamageEvent(event: PlayerItemDamageEvent) {
		if (event.item.customItem == null) return
		if (event.item.itemMeta !is Damageable) return
		event.isCancelled = true
	}

	@EventHandler(priority = EventPriority.LOWEST)
	fun playerInteractListener(event: PlayerInteractEvent) {
		if (event.hand != EquipmentSlot.HAND) return
		val item = event.item ?: return
		val customItem = item.customItem ?: return

		val listeners = getListeners(interactListeners, customItem).filter { it.preCheck(event, item) }

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, item) }
		}
	}

	@EventHandler
	fun onEntityShootBow(event: EntityShootBowEvent) {
		val offhand = event.entity.equipment?.itemInMainHand ?: return
		val customItem = offhand.customItem ?: return

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
		val customItem = itemStack.customItem ?: return

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
		val customItem = event.itemStack.customItem ?: return

		val listeners = getListeners(dispenseListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, event.itemStack) }
		}
	}
}
