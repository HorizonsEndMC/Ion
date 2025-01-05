package net.horizonsend.ion.server.features.custom.items

import com.destroystokyo.paper.event.server.ServerTickEndEvent
import io.papermc.paper.event.block.BlockPreDispenseEvent
import net.horizonsend.ion.server.command.misc.DyeCommand
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.features.custom.items.component.Listener
import net.horizonsend.ion.server.features.custom.items.component.TickReceiverModule
import net.horizonsend.ion.server.features.custom.items.type.armor.PowerArmorItem
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.minecraft.world.item.DyeItem
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.craftbukkit.util.CraftMagicNumbers
import org.bukkit.entity.LivingEntity
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.entity.EntityDamageByEntityEvent
import org.bukkit.event.entity.EntityShootBowEvent
import org.bukkit.event.inventory.PrepareItemCraftEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.event.player.PlayerItemDamageEvent
import org.bukkit.event.player.PlayerSwapHandItemsEvent
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.Damageable

object CustomItemListeners : SLEventListener() {
	// Presorted to avoid a bunch of filtering for every event at runtime
	private val interactListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerInteractEvent, *>>> = mutableMapOf()
	private val swapItemListeners: MutableMap<CustomItem, MutableSet<Listener<PlayerSwapHandItemsEvent, *>>> = mutableMapOf()
	private val dispenseListeners: MutableMap<CustomItem, MutableSet<Listener<BlockPreDispenseEvent, *>>> = mutableMapOf()
	private val entityShootBowListeners: MutableMap<CustomItem, MutableSet<Listener<EntityShootBowEvent, *>>> = mutableMapOf()
	private val craftListeners: MutableMap<CustomItem, MutableSet<Listener<PrepareItemCraftEvent, *>>> = mutableMapOf()
	private val damageEntityListeners: MutableMap<CustomItem, MutableSet<Listener<EntityDamageByEntityEvent, *>>> = mutableMapOf()

	private val tickRecievers: MutableMap<CustomItem, MutableSet<TickReceiverModule>> = mutableMapOf()

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
			components.filterIsInstance<Listener<PrepareItemCraftEvent, *>>().filterTo(getEntries(craftListeners, newCustomItem)) { it.eventType == PrepareItemCraftEvent::class }
			components.filterIsInstance<Listener<EntityDamageByEntityEvent, *>>().filterTo(getEntries(damageEntityListeners, newCustomItem)) { it.eventType == EntityDamageByEntityEvent::class }
			getEntries(tickRecievers, newCustomItem).addAll(components.filterIsInstance<TickReceiverModule>())
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

			val tickedArmorGear = mutableListOf<ItemStack?>()
			tickedArmorGear.addAll(inventory.armorContents)

			// TODO: No handheld ticked gear exists at the moment
			val tickedHandheldGear = mutableListOf<ItemStack?>()
			tickedHandheldGear.add(inventory.itemInOffHand)
			tickedHandheldGear.add(inventory.itemInMainHand)

			// Tick power armor items
			for (item in tickedArmorGear) {
				if (item == null) continue
				val customItem = item.customItem ?: continue
				val powerArmorItem = if (customItem is PowerArmorItem) customItem else continue

				val tickListeners = getEntries(tickRecievers, customItem)
				if (tickListeners.isEmpty()) continue

				for (module in tickListeners) {
					if (event.tickNumber % module.interval != 0) continue
					module.handleTick(player, item, customItem, powerArmorItem.slot)
				}
			}
		}
	}

	@EventHandler
	fun onCraftSword(event: PrepareItemCraftEvent) {
		val item = event.inventory.result ?: return
		val customItem = item.customItem ?: return
		for (listener in getEntries(craftListeners, customItem)) {
			if (!listener.preCheck(event, item)) continue
			listener.handleEvent(event, item)
		}
	}

	@EventHandler
	fun onEntityDamagedBy(event: EntityDamageByEntityEvent) {
		val damager = event.damager as? LivingEntity ?: return
		val itemInHand = damager.equipment?.itemInMainHand ?: return
		val customItem = itemInHand.customItem ?: return

		for (listener in getEntries(damageEntityListeners, customItem)) {
			if (!listener.preCheck(event, itemInHand)) continue
			listener.handleEvent(event, itemInHand)
		}
	}

	@EventHandler
	fun onEntityDamagedHolding(event: EntityDamageByEntityEvent) {
		val damaged = event.entity as? LivingEntity ?: return

		val itemInMainHand = damaged.equipment?.itemInMainHand
		val mainHandcustomItem = itemInMainHand?.customItem

		val itemInOffHand = damaged.equipment?.itemInOffHand
		val offHandcustomItem = itemInOffHand?.customItem

		if (itemInMainHand != null && mainHandcustomItem != null) for (listener in getEntries(damageEntityListeners, mainHandcustomItem)) {
			if (!listener.preCheck(event, itemInMainHand)) continue
			listener.handleEvent(event, itemInMainHand)
		}

		if (itemInOffHand != null && offHandcustomItem != null) for (listener in getEntries(damageEntityListeners, offHandcustomItem)) {
			if (!listener.preCheck(event, itemInOffHand)) continue
			listener.handleEvent(event, itemInOffHand)
		}
	}

	/*
	 * Attempt to try to allow custom items to be crafted as if they were materials.
	 * There is no loose itemstack check, only exact matches for lore, name, data, etc.
	 *
	 * Until paper accepts the PR for a predicate item requirement, this should allow matching custom item identifiers to craft together
	 **/
	@EventHandler(priority = EventPriority.LOWEST)
	fun allowLessIdealMaterials(event: PrepareItemCraftEvent) {
		// Disallow recusion from setting the items
		val trace = Thread.currentThread().stackTrace
		if (trace.any { element -> element.methodName.contains("setMatrix") }) return

		val currentItems = event.inventory.matrix
		val toReplace = mutableMapOf<ItemStack, ItemStack>()

		val stockCustomItems = Array(currentItems.size) {
			val item = currentItems[it]
			val customItem = item?.customItem
			if (customItem == null) return@Array item ?: ItemStack.empty()
			val ideal = customItem.constructItemStack(item.amount)
			if (ideal == item) return@Array item
			toReplace[item] = ideal
			ideal
		}

		val recipe = runCatching { Bukkit.getCraftingRecipe(stockCustomItems, Bukkit.getWorlds().first()) }.getOrNull()
		if (recipe == null) return

		event.inventory.matrix = Array(event.inventory.matrix.size) {
			val current = event.inventory.matrix[it] ?: return@Array null
			toReplace[current] ?: current
		}
	}

	@EventHandler
	fun allowArmorDye(event: PrepareItemCraftEvent) {
		val items = event.inventory.matrix.filterNotNullTo(mutableListOf())
		val dyes: MutableMap<ItemStack, DyeItem> = mutableMapOf()

		items.forEach {
			val itemType = CraftMagicNumbers.getItem(it.type) ?: return@forEach
			if (itemType !is DyeItem) return@forEach
			dyes[it] = itemType
		}

		if (dyes.isEmpty()) return

		items.removeAll(dyes.keys)

		val dyeable = items.firstOrNull { stack -> DyeCommand.canHexDye(stack) } ?: return

		items.remove(dyeable)

		// Other items in the grid
		if (items.isNotEmpty()) return

		// This mutates the item, so use a clone
		val dyed = DyeCommand.applyDye(dyeable.clone(), dyes.values)

		event.inventory.result = dyed
	}
}
