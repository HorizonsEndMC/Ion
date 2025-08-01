package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.*
import net.horizonsend.ion.common.database.schema.misc.PlayerSettings
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.getSetting
import net.horizonsend.ion.server.features.cache.PlayerSettingsCache.setSetting
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayCurrentBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayItem
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.customItem
import net.horizonsend.ion.server.listener.misc.ProtectionListener
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.block.data.type.Chest
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

@CommandAlias("itemsearch")
@CommandPermission("ion.search")
object SearchCommand : SLCommand() {
	@Default
	@CommandCompletion("@anyItem")
	fun default(player: Player, vararg itemStrings: String) {
		// key: <Block> (for location)
		// value: <Inventory> (for inventory contents)
		val containers = mutableMapOf<Block, Inventory>()

		val stringList = itemStrings.toList()

		if (stringList.isEmpty()) {
			val item = player.inventory.itemInMainHand
			val str = GlobalCompletions.toItemString(item)
			containers.putAll(findContainers(player, player.world, player.location, item, str))
		} else {
			for (str in stringList) {
				val item = GlobalCompletions.stringToItem(str) ?: continue
				containers.putAll(findContainers(player, player.world, player.location, item, str))
			}
		}
		val blocks = containers.keys
		val inventories = containers.values.toMutableSet()

		Tasks.async {
			if (itemStrings.isEmpty()) { // player typed "/itemsearch"
				if (player.inventory.itemInMainHand.type == Material.AIR) { // empty hand
					player.userError("Specify which item to search.")
					return@async
				} else {
					val item = player.inventory.itemInMainHand // non-empty hand
					searchItem(item, player, blocks, inventories, stringList)
				}
			} else {
				for (str in stringList) { // strList isn't empty
					val item = GlobalCompletions.stringToItem(str) ?: continue
					searchItem(item, player, blocks, inventories, stringList)
				}
			}

			if (inventories.isEmpty()) {
				player.userError("Couldn't find any item in a 20 block range.")
			} else {
				player.sendRichMessage(
					"<hover:show_text:\"${blocks.joinToString("\n") { "<gray>X: ${it.x} Y: ${it.y} Z: ${it.z}" }}\">" +
						"<#7f7fff>Found ${blocks.size} inventories. [Hover]"
				)
			}
		}
	}

	@Subcommand("_toggle")
	fun itemSearchToggle(player: Player, @Optional toggle: Boolean?) {
		val showItemDisplay = toggle ?: !player.getSetting(PlayerSettings::showItemSearchItem)
		player.setSetting(PlayerSettings::protectionMessagesEnabled, showItemDisplay)

		player.success("Changed showing searched item to $showItemDisplay")
	}

	/**
	 * Searches for an item in a list of containers and displays the results as display entities
	 */
	private fun searchItem(item: ItemStack, player: Player, blocks: MutableSet<Block>, inventories: MutableSet<Inventory>, strList: List<String>) {
		for (block in blocks.withIndex()) {
			val loc = Vector(block.value.x.toDouble(), block.value.y.toDouble(), block.value.z.toDouble())

			if (!containsItem(player, inventories.elementAt(block.index), item)) continue //necessary check for multi-item searches to prevent false positives

			if ((twoOrMoreMatches(player, inventories.elementAt(block.index), strList)) || // if container has 2+ of the searched items
				(item.type.isBlock && item.type.isSolid) || // Billboarding blocks looks so messed up, so this mostly prevents that
				item.type == Material.AIR || // display if item is air, otherwise it would show up as an invisible item
				!player.getSetting(PlayerSettings::showItemSearchItem)
			) // toggleable setting
			{
				sendEntityPacket(player, displayCurrentBlock(player.world.minecraft, loc), 10 * 20) // show block
			} else {
				sendEntityPacket(player, displayItem(player, item, loc), 10 * 20) // show item
			}
		}
	}

	/**
	 * Finds all inventories that are holding the specified item, in a specified distance around a center point.
	 */
	private fun findContainers(
		searcher: Player,
		world: World,
		loc: Location,
		item: ItemStack,
		itemStr: String,
		dist: Int = 20
	): MutableMap<Block, Inventory> {
		val map = mutableMapOf<Block, Inventory>()
		for (x in loc.x.toInt() - dist..loc.x.toInt() + dist) {
			for (y in loc.y.toInt() - dist..loc.y.toInt() + dist) {
				for (z in loc.z.toInt() - dist..loc.z.toInt() + dist) {
					val block = world.getBlockAt(x, y, z)
					val inv = (block.state as? InventoryHolder)?.inventory ?: continue
					if (block.type == Material.CHEST) { // one of the blocks of a double chest
						val data = (block.blockData as Chest).type
						if (chestContainsItem(searcher, inv, item, data) || (itemStr == "AIR" && containsAir(inv))) {
							map[block] = inv
						}
					} else if (containsItem(searcher, inv, item) || (itemStr == "AIR" && containsAir(inv))) {
						map[block] = inv
					}
				}
			}
		}
		return map
	}

	/**
	 * Determines if 2 items match
	 */
	private fun itemsMatch(item1: ItemStack, item2: ItemStack): Boolean {
		if (item1.type == item2.type) {
			return item1.customItem == item2.customItem
		}

		return false
	}

	/**
	 * Determines if a Chest contains a specified item
	 * Also handles logic for double chests, returning the proper half
	 */
	private fun chestContainsItem(searcher: Player, inventory: Inventory, item: ItemStack, type: Chest.Type): Boolean {
		val startIndex: Int
		val stopIndex: Int
		when (type) {
			Chest.Type.RIGHT -> {
				startIndex = 0
				stopIndex = 26
			}

			Chest.Type.LEFT -> {
				startIndex = 27
				stopIndex = 53
			}

			else -> {
				return containsItem(searcher, inventory, item)
			}
		}
		for (i in startIndex..stopIndex) {
			val invItem = inventory.getItem(i) ?: continue
			if (itemsMatch(invItem, item)) return true
		}
		return false
	}

	/**
	 * Function that compares the specified item's Material and Custom Model Data with every item in a searched inventory
	 * (because comparing custom item stacks to modified custom items [like PA vs. PA with modules] will not work)
	 */
	private fun containsItem(player: Player, inventory: Inventory, itemStack: ItemStack): Boolean {
		for (item in inventory) {
			if (item == null) continue

			val loc = inventory.location ?: continue // If location is null, then it is a virtual inventory and shouldn't be searched anyways
			if (ProtectionListener.isRegionDenied(player, loc)) continue

			if (itemsMatch(item, itemStack)) return true
		}
		return false
	}

	/**
	 * Function that searches an Inventory and returns true if it contains any empty slots
	 * basically just !isFull(inventory) if an isFull() function existed
	 */
	private fun containsAir(inventory: Inventory): Boolean {
		for (item in inventory) {
			if (item == null) return true
		}
		return false
	}

	/**
	 * Returns true if two or more items in the list of strings exist within the inventory
	 */
	private fun twoOrMoreMatches(searcher: Player, inventory: Inventory, strList: List<String>): Boolean {
		var counter = 0
		for (str in strList) {
			val itemStack = GlobalCompletions.stringToItem(str) ?: continue
			if (containsItem(searcher, inventory, itemStack)) {
				counter++
			}
		}
		return counter > 1
	}
}
