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
		when (event.slot) {
			45 -> if (pageNumber > 0) pageNumber-- else return
			53 -> if (pageNumber < Bukkit.getOnlinePlayers().size / 27) pageNumber++ else return
			else -> return
		}
		placeBounties()
	}

	private fun placeBounties() {
		val grayGlassFiller: Array<Int> = arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 46, 47, 48, 49, 50, 51, 52)
		grayGlassFiller.forEach { inventory.setItem(it, ItemStack(Material.GRAY_STAINED_GLASS_PANE)) }
		val blackGlassFiller: Array<Int> = arrayOf(9, 18, 27, 36, 17, 26, 35, 44)
		blackGlassFiller.forEach { inventory.setItem(it, ItemStack(Material.BLACK_STAINED_GLASS_PANE)) }

		val bounties: MutableList<Player> = Bukkit.getOnlinePlayers().toMutableList()
		if (pageNumber > 0) {
			if (inventory.getItem(45) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(105)
				}

				inventory.setItem(45, leftArrow)
			}
		} else {
			inventory.setItem(45, ItemStack(Material.GRAY_STAINED_GLASS_PANE))
		}

		if (pageNumber < bounties.size / 27) {
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

		if (pageNumber <= 1) {
			for (i in 0..53) {
				val slot = inventory.getItem(i)
				if (slot == null && bounties.isNotEmpty()) {
					val currentBounty = bounties.removeAt(0)
					val head = ItemStack(Material.PLAYER_HEAD)
					head.editMeta(SkullMeta::class.java) {
						it.displayName(
							MiniMessage.miniMessage()
								.deserialize("<red>${currentBounty.name}<red>: <gray>${PlayerData[currentBounty.uniqueId].bounty}<gray>")
						)
						it.owningPlayer = currentBounty
					}
					Companion.bounties[currentBounty] = mutableMapOf(pageNumber to i)
					inventory.setItem(i, head)
				} else {
					continue
				}
			}
		}
		if (pageNumber > 1 && bounties.isNotEmpty()) {
			for (i in 0..53) {
				val slot = inventory.getItem(i)
				if (slot == null) {
					val currentBounty = bounties.removeAt(0)
					if (PlayerData[currentBounty.uniqueId].bounty == 0) continue
					val head = ItemStack(Material.PLAYER_HEAD)
					head.editMeta(SkullMeta::class.java) {
						it.displayName(
							MiniMessage.miniMessage()
								.deserialize("<red>$currentBounty<red>: <gray>${PlayerData[currentBounty.uniqueId].bounty}<gray>")
						)
						it.owningPlayer = currentBounty
					}
					inventory.setItem(i, head)
					Companion.bounties[currentBounty] = mutableMapOf(pageNumber to i)
				} else {
					continue
				}
			}
		}
	}

	companion object {
		var bounties: MutableMap<Player, MutableMap<Int, Int>> = mutableMapOf()
	}
}
