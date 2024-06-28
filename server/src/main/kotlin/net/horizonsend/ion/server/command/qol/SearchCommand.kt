package net.horizonsend.ion.server.command.qol

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayCurrentBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayItem
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.setValue

@CommandAlias("itemsearch")
@CommandPermission("ion.search")
object SearchCommand : SLCommand() {
	@Default
	@CommandCompletion("@anyItem")
	fun default( player: Player, vararg itemStrList: String) {
		val strList = itemStrList.toList()
		val containers = mutableSetOf<InventoryHolder>()
		val containerBlocks = mutableSetOf<Block>()
		for(str in strList) {
			val item = GlobalCompletions.stringToItem(str) ?: player.inventory.itemInMainHand
			val tempContainers = findContainers(player.world, player.location, item, str) //<InventoryHolder> (for inventory contents)
			containers.addAll(tempContainers)

			//This is just so I stop trying to cast block state stuff async
			//(I know there's a better way to do it, but I don't know what it is)
			val tempContainerBlocks = findContainerBlocks(player.world, player.location, item, str) //<Block> (for location)
			containerBlocks.addAll(tempContainerBlocks)
		}
		Tasks.async {
			for (str in strList) {
				val item = GlobalCompletions.stringToItem(str) ?: player.inventory.itemInMainHand
				for (block in containerBlocks.withIndex()) {
					val loc = Location( player.world, block.value.x.toDouble(), block.value.y.toDouble(), block.value.z.toDouble() )

					if(!containsItem(containers.elementAt(block.index).inventory, item)) continue //necessary check for multi-item searches to prevent false positives

					if (twoOrMoreMatches(containers.elementAt(block.index), strList) || // if container has 2+ of the searched items
						(item.type.isBlock && item.type.isSolid) || // Billboarding blocks looks so messed up, so this mostly prevents that
						str == "AIR" || // display if item is air, otherwise it would show up as an invisible item
						!PlayerCache[player].showItemSearchItem) // toggleable setting
					{
						sendEntityPacket(player, displayCurrentBlock(player.world.minecraft, loc.toVector()), 10 * 20) // show block
					} else {
						sendEntityPacket(player, displayItem(player, item, loc.toVector()), 10 * 20) // show item
					}
				}
			}
			if (itemStrList.isEmpty()) {
				player.userError("Specify which item to search.")
			} else if (containers.size == 0) {
				player.userError("Couldn't find any item in a 20 block range.")
			} else {
				player.sendRichMessage(
					"<hover:show_text:\"${containerBlocks.joinToString("\n") { "<gray>X: ${it.x} Y: ${it.y} Z: ${it.z}" }}\">" +
						"<#7f7fff>Found ${containerBlocks.size} containers. [Hover]"
				)
			}
		}
	}

	@Subcommand("_toggle")
	fun itemSearchToggle(player: Player, @Optional toggle: Boolean?) {
		val showItemDisplay = toggle ?: !PlayerCache[player].showItemSearchItem
		SLPlayer.updateById(player.slPlayerId, setValue(SLPlayer::showItemSearchItem, showItemDisplay))
		PlayerCache[player.uniqueId].showItemSearchItem = showItemDisplay
		player.success("Changed showing searched item to $showItemDisplay")
	}

	/**
	 * Finds all containers that are holding the specified item, in a radius of 20 blocks around a center point.
	 * @param world The world that the containers are in.
	 * @param loc The Location of the center block
	 * @param item The item being searched
	 * @param radius The search radius
	 */
	private fun findContainers(
		world: World,
		loc: Location,
		item: ItemStack,
		itemStr: String,
		radius: Int = 20
	) : MutableSet<InventoryHolder> {
		val blockSet = mutableSetOf<InventoryHolder>()
		for(x in loc.x.toInt() - radius..loc.x.toInt() + radius){
			for(y in loc.y.toInt()-radius .. loc.y.toInt()+radius){
				for(z in loc.z.toInt()-radius .. loc.z.toInt()+radius){
					val block = world.getBlockAt(x, y, z)
					val inv = block.state as? InventoryHolder ?: continue
					//added second conditional cause there are no "AIR" item stacks
					if( containsItem(inv.inventory, item) || (itemStr == "AIR" && containsAir(inv.inventory))) {
						blockSet.add(inv)
					}
				}
			}
		}
		return blockSet
	}

	/**
	 * its literally just findContainer() but returns the block instead of the InventoryHolder so that you can access coordinates async
	 * @param world The world that the containers are in.
	 * @param loc The Location of the center block
	 * @param item The item being searched
	 * @param itemStr The inputted String of the item being searched
	 * @param radius The search radius
	 */
	private fun findContainerBlocks(
		world: World,
		loc: Location,
		item: ItemStack,
		itemStr: String,
		radius: Int = 20
	) : MutableSet<Block> {
		val blockSet = mutableSetOf<Block>()
		for(x in loc.x.toInt() - radius..loc.x.toInt() + radius){
			for(y in loc.y.toInt()-radius .. loc.y.toInt()+radius){
				for(z in loc.z.toInt()-radius .. loc.z.toInt()+radius){
					val block = world.getBlockAt(x, y, z)
					val inv = block.state as? InventoryHolder ?: continue
					if(containsItem(inv.inventory, item) || (itemStr == "AIR" && containsAir(inv.inventory))) {
						blockSet.add(block)
					}
				}
			}
		}
		return blockSet
	}

	/**
	 * Function that searches an Inventory and returns true if it contains any empty slots
	 * basically just !isFull(inventory) if an isFull() function existed
	 * @param inventory the inventory being searched
	 */
	private fun containsAir(inventory: Inventory): Boolean {
		for (item in inventory) {
			if (item == null) return true
		}
		return false
	}

	/**
	 * Function that compares the specified item's Material and Custom Model Data with every item in a searched inventory
	 * (because comparing custom item stacks to modified custom items [like PA vs. PA with modules] will not work)
	 * @param inventory the inventory being searched
	 * @param itemStack the item that's being searched
	 */
	private fun containsItem(inventory: Inventory, itemStack: ItemStack): Boolean {
		for(item in inventory){
			if(item == null) continue
			if(item.type == itemStack.type) {
				if ( !(item.itemMeta.hasCustomModelData() && itemStack.itemMeta.hasCustomModelData()) ) return true //if both don't have custom item data, return true
				else if (item.itemMeta.customModelData == itemStack.itemMeta.customModelData) return true //if both do, and they both match, return true
			}
		}
		return false
	}

	/**
	 * Returns true if two or more items in the list of strings exist within the inventory
	 * @param inventoryHolder the inventory being searched
	 * @param strList the list of strings that are being compared
	 */
	private fun twoOrMoreMatches(inventoryHolder: InventoryHolder, strList: List<String>): Boolean {
		var counter = 0
		for(str in strList){
			val itemStack = GlobalCompletions.stringToItem(str) ?: continue
			if(containsItem(inventoryHolder.inventory, itemStack)){
				counter++
			}
		}
		return counter > 1
	}
}
