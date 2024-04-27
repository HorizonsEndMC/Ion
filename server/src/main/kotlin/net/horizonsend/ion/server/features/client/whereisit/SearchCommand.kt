package net.horizonsend.ion.server.features.client.whereisit

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayCurrentBlock
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.displayItem
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities.sendEntityPacket
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.Location
import org.bukkit.World
import org.bukkit.block.Block
import org.bukkit.entity.Player
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack

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
			val tempContainers = findContainers(player.world, player.location, item, str)
			containers.addAll(tempContainers)
			//this is just so I stop trying to cast block state stuff async
			val tempContainerBlocks = findContainerBlocks(player.world, player.location, item, str)
			containerBlocks.addAll(tempContainerBlocks)
		}
		Tasks.async {
			for (str in strList) {
				val item = GlobalCompletions.stringToItem(str) ?: player.inventory.itemInMainHand
				for (block in containerBlocks.withIndex()) {
					val loc = Location( player.world, block.value.x.toDouble(), block.value.y.toDouble(), block.value.z.toDouble() )
					if (twoOrMoreMatches(containers.elementAt(block.index), strList) || // if container has 2+ of the searched items
						(item.type.isBlock && item.type.isSolid) || // Billboarding blocks looks so messed up
						str == "AIR") // People want to see empty slots, so I added it
					{
						sendEntityPacket(player, displayCurrentBlock(player.world.minecraft, loc.toVector()), 10 * 20)
					} else {
						if(containers.elementAt(block.index).inventory.contains(item)) //necessary check for multi-item searches to prevent multiples
							sendEntityPacket(player, displayItem(player, item, loc.toVector()), 10 * 20)
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
					if(inv.inventory.contains(item) || (itemStr == "AIR" && containsAir(inv.inventory))) {
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
					//added second conditional cause there are no "AIR" item stacks
					if(inv.inventory.contains(item) || (itemStr == "AIR" && containsAir(inv.inventory))) {
						blockSet.add(block)
					}
				}
			}
		}
		return blockSet
	}

	private fun containsAir(inventory: Inventory): Boolean {
		for (item in inventory) {
			if (item == null) return true
		}
		return false
	}

	private fun twoOrMoreMatches(inventoryHolder: InventoryHolder, strList: List<String>): Boolean {
		var counter = 0
		for(str in strList){
			val itemStack = GlobalCompletions.stringToItem(str)
			for(item in inventoryHolder.inventory){
				if(inventoryHolder.inventory.contains(itemStack)){
					counter++
					break
				}
			}
		}
		return counter > 1
	}
}
