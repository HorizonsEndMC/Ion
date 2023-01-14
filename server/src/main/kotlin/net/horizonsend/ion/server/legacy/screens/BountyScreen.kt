package net.horizonsend.ion.server.legacy.screens

import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.server.legacy.utilities.acceptBounty
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.SkullMeta

class BountyScreen : Screen(Bukkit.createInventory(null, 54)) {
	private var pageNumber: Int = 0

	init {
		placeBounties()
	}

	override fun handleInventoryClick(event: InventoryClickEvent) {
		for (it in bounties) {
			for (pageNumberToIndex in it.value) {
				if (pageNumber == pageNumberToIndex.key && event.slot == pageNumberToIndex.value) {
					(event.whoClicked as? Player)?.acceptBounty(it.key)
					return
				} else {
					continue
				}
			}
		}
		// this is to change the pages, 45 is bottom left corner, and goes back a page, 53 is bottom right corner and goes foward a page
		when (event.slot) {
			45 -> if (pageNumber > 0) pageNumber-- else return
			53 -> if (pageNumber < Bukkit.getOnlinePlayers().size / 27) pageNumber++ else return
			else -> return
		}
		placeBounties()
	}

	private fun placeBounties() {
		// This is the border glass on the top & bottom
		val grayGlassFiller: Array<Int> = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 46, 47, 48, 49, 50, 51, 52)
		grayGlassFiller.forEach { inventory.setItem(it, ItemStack(Material.GRAY_STAINED_GLASS_PANE)) }
		// This is the border glass on the sides
		val blackGlassFiller: Array<Int> = arrayOf(9, 18, 27, 36, 17, 26, 35, 44)
		blackGlassFiller.forEach { inventory.setItem(it, ItemStack(Material.BLACK_STAINED_GLASS_PANE)) }
		// a list of all the online players
		val onlinePlayers: MutableList<Player> = Bukkit.getOnlinePlayers().toMutableList()
		// place an arrow in the left corner, if not on the first page
		if (pageNumber > 0) {
			val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
			leftArrow.editMeta {
				it.displayName(Component.empty())
				it.setCustomModelData(105)
			}
			inventory.setItem(45, leftArrow)
		} else {
			inventory.setItem(45, ItemStack(Material.GRAY_STAINED_GLASS_PANE))
		}
		// if the number of players is bigger then what the inventory can store add in the right arrow, to switch to the next page
		if (pageNumber < onlinePlayers.size / 27) {
			if (inventory.getItem(53) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(103)
				}

				inventory.setItem(53, leftArrow)
			}
		} else {
			inventory.setItem(53, ItemStack(Material.GRAY_STAINED_GLASS_PANE))
		}
		// place the heads
		if (pageNumber <= 1) {
			// for all the slots in the inventory
			for (i in 0..53) {
				val slot = inventory.getItem(i)
				if (slot == null && onlinePlayers.isNotEmpty()) {
					// remove the players from the online players, and save it to currentPlayer
					val currentPlayer = onlinePlayers.removeAt(0)
					val head = ItemStack(Material.PLAYER_HEAD)
					head.editMeta(SkullMeta::class.java) {
						it.displayName(
							MiniMessage.miniMessage()
								.deserialize("<red>${currentPlayer.name}<red>: <gray>${PlayerData[currentPlayer.uniqueId].bounty}<gray>")
						)
						// add the skin to the head
						it.owningPlayer = currentPlayer
					}
					// add the currentPlayer to the list of all possible available bounties.
					Companion.bounties[currentPlayer] = mutableMapOf(pageNumber to i)
					// add the head to the inventory
					inventory.setItem(i, head)
				} else {
					continue
				}
			}
		}
		if (pageNumber > 1 && onlinePlayers.isNotEmpty()) {
			// for every slot in the inventory
			for (i in 0..53) {
				val slot = inventory.getItem(i)
				if (slot == null) {
					val currentPlayer = onlinePlayers.removeAt(0)
					if (PlayerData[currentPlayer.uniqueId].bounty == 0) continue
					val head = ItemStack(Material.PLAYER_HEAD)
					head.editMeta(SkullMeta::class.java) {
						it.displayName(
							MiniMessage.miniMessage()
								.deserialize("<red>$currentPlayer<red>: <gray>${PlayerData[currentPlayer.uniqueId].bounty}<gray>")
						)
						it.owningPlayer = currentPlayer
					}
					inventory.setItem(i, head)
					Companion.bounties[currentPlayer] = mutableMapOf(pageNumber to i)
				} else {
					continue
				}
			}
		}
	}

	companion object {
		// list of all current possible to claim bounties, map of Player to map of page number to slot number
		var bounties: MutableMap<Player, MutableMap<Int, Int>> = mutableMapOf()
	}
}
