package net.horizonsend.ion.server.features.custom.items

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.component.TickRecievierModule
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object NewCustomItemListeners : SLEventListener() {
	// Presorted to avoid a bunch of filtering for every event at runtime
	private val interactListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerInteractEvent, *>>> = mutableMapOf()
	private val swapItemListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerSwapHandItemsEvent, *>>> = mutableMapOf()
	private val dispenseListeners: MutableMap<CustomItem, MutableSet<Listener<BlockPreDispenseEvent, *>>> = mutableMapOf()
	private val entityShootBowListeners: MutableMap<CustomItem, MutableSet<Listener<EntityShootBowEvent, *>>> = mutableMapOf()

	private val tickRecievers: MutableMap<CustomItem, MutableSet<TickRecievierModule>> = mutableMapOf()

	private fun <T: CustomItem, Z: Any> getEntries(
		collection: MutableMap<CustomItem, MutableSet<Z>>,
		item: T
	): MutableSet<Z> {
		return collection.getOrPut(item) { mutableSetOf() }
	}

	fun sortCustomItemListeners() {
		for (newCustomItem in CustomItemRegistry.ALL) {
			val components = newCustomItem.allComponents()

			components.filterIsInstance<Listener<PlayerInteractEvent, *>>().filterTo(getEntries(interactListeners, newCustomItem)) { it.eventType == PlayerInteractEvent::class }
			components.filterIsInstance<Listener<PlayerSwapHandItemsEvent, *>>().filterTo(getEntries(swapItemListeners, newCustomItem)) { it.eventType == PlayerSwapHandItemsEvent::class }
			components.filterIsInstance<Listener<BlockPreDispenseEvent, *>>().filterTo(getEntries(dispenseListeners, newCustomItem)) { it.eventType == BlockPreDispenseEvent::class }
			components.filterIsInstance<Listener<EntityShootBowEvent, *>>().filterTo(getEntries(entityShootBowListeners, newCustomItem)) { it.eventType == EntityShootBowEvent::class }
			getEntries(tickRecievers, newCustomItem).addAll(components.filterIsInstance<TickRecievierModule>())
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

		val listeners = getEntries(interactListeners, customItem).filter { it.preCheck(event, item) }

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, item) }
		}
	}

	@EventHandler
	fun onEntityShootBow(event: EntityShootBowEvent) {
		val offhand = event.entity.equipment?.itemInMainHand ?: return
		val customItem = offhand.customItem ?: return

		val listeners = getEntries(entityShootBowListeners, customItem)

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

		val listeners = getEntries(swapItemListeners, customItem)

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

		val listeners = getEntries(dispenseListeners, customItem)

		if (listeners.isNotEmpty()) {
			event.isCancelled = true
			listeners.forEach { it.handleEvent(event, event.itemStack) }
		}
	}

	@EventHandler
	fun onServerTickEnd(event: ServerTickEndEvent) = Tasks.async {
		for (player in Bukkit.getOnlinePlayers()) {
			val inventory = player.inventory

			val tickedGear = mutableListOf<ItemStack?>()
			tickedGear.addAll(inventory.armorContents)
			tickedGear.add(inventory.itemInOffHand)
			tickedGear.add(inventory.itemInMainHand)

			for (item in tickedGear) {
				if (item == null) continue
				val customItem = item.customItem
				if (customItem == null) continue

				val tickListeners = getEntries(tickRecievers, customItem)
				if (tickListeners.isEmpty()) continue

				for (module in tickListeners) {
					if (event.tickNumber % module.interval != 0) continue
					module.handleTick(player, item, customItem)
				}
			}
		}
	}
}
