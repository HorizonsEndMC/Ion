package net.starlegacy.feature.economy.cargotrade

import net.starlegacy.SLComponent
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.event.StarshipPilotedEvent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.util.action
import org.bukkit.ChatColor.GREEN
import org.bukkit.ChatColor.RED
import org.bukkit.ChatColor.RESET
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import org.bukkit.block.ShulkerBox
import org.bukkit.block.data.Directional
import org.bukkit.block.data.type.Piston
import org.bukkit.entity.Player
import org.bukkit.event.Event
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.block.BlockPistonExtendEvent
import org.bukkit.event.block.BlockPistonRetractEvent
import org.bukkit.event.block.BlockPlaceEvent
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemDespawnEvent
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.event.inventory.InventoryDragEvent
import org.bukkit.event.inventory.InventoryMoveItemEvent
import org.bukkit.event.inventory.InventoryOpenEvent
import org.bukkit.event.inventory.InventoryPickupItemEvent
import org.bukkit.event.inventory.InventoryType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryView
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.PlayerInventory

object CrateRestrictions : SLComponent() {
	private val CRATE_HOLDER = Material.STICKY_PISTON
	private const val MIN_FREE_SPACE = 1

	// Limits for placing crates
	@EventHandler(priority = EventPriority.HIGHEST)
	fun onPlace(event: BlockPlaceEvent) {
		if (!event.canBuild()) return

		val block: Block = event.block
		val state: ShulkerBox = block.state as? ShulkerBox ?: return

		// Override cancelling from other plugins. It will only not be cancelled if the below conditions are met.
		// If it is not cancelled here, it should bypass protection.
		event.isCancelled = true

		CargoCrates[state] ?: return // don't need to store it, just check if is a crate

		val against = event.blockAgainst
		val direction = against.getFace(block)

		if (against.type != CRATE_HOLDER || (against.state.blockData as Directional).facing != direction) {
			event.player.sendActionBar("${RED}You can only place cargo crates against retracted sticky piston heads!")
			return
		}

		for (i in 1..MIN_FREE_SPACE) {
			if (block.getRelative(direction, i).type != Material.AIR) {
				event.player.sendActionBar("${RED}Must be at least $MIN_FREE_SPACE block(s) of space beyond the crate!")
				return
			}
		}

		for (x in -1..1) {
			for (y in -1..1) {
				for (z in -1..1) {
					val relative = block.getRelative(x, y, z)
					if (relative == block) continue
					if (relative.state is ShulkerBox) {
						event.player.sendActionBar("${RED}Cargo crates cannot be placed adjacently!")
						return
					}
				}
			}
		}

		event.isCancelled = false
		event.player.sendActionBar("${RESET}Placed ${state.customName}".replace("$RESET", "$GREEN"))
	}

	//region Piston State Change Crate Popping

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	fun onBreak(event: BlockBreakEvent) {
		val block = event.block
		onPistonChange(block)
	}

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	fun postMove(event: BlockPistonExtendEvent) = event.blocks.forEach(this::onPistonChange)

	@EventHandler(ignoreCancelled = true, priority = EventPriority.MONITOR)
	fun postMove(event: BlockPistonRetractEvent) = event.blocks.forEach(this::onPistonChange)

	private fun onPistonChange(block: Block) {
		if (block.type != CRATE_HOLDER) {
			return
		}

		val relative = block.getRelative((block.blockData as Piston).facing)

		if (relative.state !is ShulkerBox) {
			return
		}

		relative.breakNaturally()
	}

	//endregion Piston State Change Crate Popping

	//region Inventory Exploit Protection

	// Prevent moving items... somehow
	@EventHandler(ignoreCancelled = true)
	fun onInventoryMove(event: InventoryMoveItemEvent) {
		val crate = CargoCrates[event.item] ?: return

		event.isCancelled = true

		if (event.source !is PlayerInventory) log.warn(
			"${event.initiator.viewers.joinToString { it.name }} had a crate $crate" +
				" at ${event.source.location} in a ${event.source.type} (InventoryMoveItemEvent)"
		)
	}

	// Prevent clicking on a slot to place item
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onInventoryDrag(event: InventoryDragEvent) {
		if (noneAreCrates(event.cursor, event.oldCursor)) return
		if (isPlayerOnly(event.view)) return
		event.isCancelled = true
		event.result = Event.Result.DENY
	}

	// Prevent shift clicking etc.
	@EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
	fun onInventoryClick(event: InventoryClickEvent) {
		val hotbarItem = event.hotbarButton.takeIf { it != -1 }?.let { event.whoClicked.inventory.getItem(it) }

		if (noneAreCrates(event.currentItem, event.cursor, hotbarItem)) {
			return
		}

		if (isPlayerOnly(event.view)) return
		event.isCancelled = true
		event.result = Event.Result.DENY
	}

	private fun noneAreCrates(vararg items: ItemStack?) = items.all { CargoCrates[it] == null }

	private fun isPlayerOnly(view: InventoryView): Boolean {
		val topInventory: Inventory? = view.topInventory
		val bottomInventory: Inventory? = view.bottomInventory

		val inventories: List<InventoryType?> = listOf(topInventory?.type, bottomInventory?.type)

		return inventories.all { it == null || it == InventoryType.PLAYER || it == InventoryType.CRAFTING }
	}

	// Don't let people open shulkers
	@EventHandler
	fun onShulkerOpen(event: InventoryOpenEvent) {
		if (event.inventory.type == InventoryType.SHULKER_BOX) {
			event.isCancelled = true
		}
	}

	//endregion Inventory Exploit Protection

	//region Hopper Exploit Prevention

	// Don't let hoppers pick up crates
	@EventHandler(ignoreCancelled = true)
	fun onHopperPickup(event: InventoryPickupItemEvent) {
		CargoCrates[event.item.itemStack] ?: return
		event.isCancelled = true
	}

	// Prevent taking items out of shulkers
	@EventHandler
	fun onShulkerExport(event: InventoryMoveItemEvent) {
		if (event.source.holder !is ShulkerBox) return
		event.isCancelled = true
	}

	// Prevent putting items into shulkers
	@EventHandler
	fun onShulkerImport(event: InventoryMoveItemEvent) {
		if (event.destination.holder !is ShulkerBox) {
			return
		}

		event.isCancelled = true
	}

	// If someone somehow gets this item through hoppers, make the hoppers explode and print a warning!
	@EventHandler(priority = EventPriority.HIGH, ignoreCancelled = false)
	fun onHopperMove(event: InventoryMoveItemEvent) {
		val crate = CargoCrates[event.item] ?: return
		val blockState = event.source.holder as? BlockState ?: return

		event.isCancelled = true
		blockState.world.createExplosion(blockState.location, 4.0f)

		log.warn("!!! Someone had a crate $crate at ${blockState.location} !!!")
	}

	//endregion Hopper Exploit Prevention

	//region Starship Exploit Prevention
	@EventHandler
	fun onPilot(event: StarshipPilotedEvent) {
		dropPassengerCrates(event.starship)
	}

	@EventHandler
	fun onPilot(event: StarshipTranslateEvent) {
		dropPassengerCrates(event.starship)
	}

	fun dropPassengerCrates(starship: ActiveStarship) {
		val world = starship.world

		for (player in world.players) {
			if (!starship.isWithinHitbox(player)) {
				continue
			}

			val inventory = player.inventory

			for (item: ItemStack? in inventory.contents) {
				if (item == null) {
					continue
				}

				if (CargoCrates[item] == null) {
					continue
				}

				inventory.removeItemAnySlot(item)
				world.dropItemNaturally(player.location, item)
			}
		}
	}

	@EventHandler
	fun onPickup(event: EntityPickupItemEvent) {
		val item = event.item.itemStack

		if (CargoCrates[item] == null) {
			return
		}

		val entity = event.entity
		val world = entity.world

		if (ActiveStarships.getInWorld(world).none { it.isWithinHitbox(entity) }) {
			return
		}

		event.isCancelled = true
		(entity as? Player)?.action("&cYou cannot pick up crates inside a piloted ship!")
	}
	//endregion

	//region Misc
	/**
	 * Cancels despawning of crate items
	 */
	@EventHandler
	fun onDespawn(event: ItemDespawnEvent) {
		if (CargoCrates[event.entity.itemStack] != null && event.entity.ticksLived < 20 * 60 * 60) {
			event.isCancelled = true
		}
	}
	//endregion
}
