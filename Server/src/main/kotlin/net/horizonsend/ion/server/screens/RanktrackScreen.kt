package net.horizonsend.ion.server.screens

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.database.collections.PlayerData
import net.horizonsend.ion.common.database.enums.Ranktrack
import net.horizonsend.ion.server.utilities.calculateRank
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack

class RanktrackScreen private constructor(
	private val targetName: String,
	private val targetsRanktrack: Ranktrack.Rank
) : TextScreen(buildPageText(targetName)) {
	private var pageNumber: Int = 0

	constructor(targetName: String) : this(targetName, calculateRank(PlayerData[targetName]!!))

	init {
		placeGui()
	}

	private var currentRanktrack: Ranktrack = Ranktrack.REFUGEE

	override fun handleInventoryClick(event: InventoryClickEvent) {
		when (event.slot) {
			//exit button to main menu
			0 -> if (pageNumber >= 1) {
				placeGui((event.whoClicked as? CraftPlayer)); pageNumber = 0
			} else return
			//switching between ranktrack screens
			//Outlaw
			9 -> if (pageNumber < 1) {
				pageNumber++; placeRanktrackScreen(Ranktrack.OUTLAW, (event.whoClicked as CraftPlayer))
			}
			11 -> if (pageNumber < 1) {
				pageNumber = 1; placeRanktrackScreen(Ranktrack.OUTLAW, (event.whoClicked as CraftPlayer))
			}
			12 -> if (pageNumber < 1) {
				pageNumber = 2; placeRanktrackScreen(Ranktrack.OUTLAW, (event.whoClicked as CraftPlayer))
			}
			13 -> if (pageNumber < 1) {
				pageNumber = 3; placeRanktrackScreen(Ranktrack.OUTLAW, (event.whoClicked as CraftPlayer))
			}
			15 -> if (pageNumber < 1) {
				pageNumber = 4; placeRanktrackScreen(Ranktrack.OUTLAW, (event.whoClicked as CraftPlayer))
			}
			//Industrialist
			18 -> if (pageNumber < 1) {
				pageNumber++; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			20 -> if (pageNumber < 1) {
				pageNumber = 1; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			21 -> if (pageNumber < 1) {
				pageNumber = 2; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			22 -> if (pageNumber < 1) {
				pageNumber = 3; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			23 -> if (pageNumber < 1) {
				pageNumber = 4; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			24 -> if (pageNumber < 1) {
				pageNumber = 5; placeRanktrackScreen(Ranktrack.INDUSTRIALIST, (event.whoClicked as CraftPlayer))
			}
			//Privateer
			27 -> if (pageNumber < 1) {
				pageNumber++; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			29 -> if (pageNumber < 1) {
				pageNumber = 1; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			30 -> if (pageNumber < 1) {
				pageNumber = 2; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			31 -> if (pageNumber < 1) {
				pageNumber = 3; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			32 -> if (pageNumber < 1) {
				pageNumber = 4; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			33 -> if (pageNumber < 1) {
				pageNumber = 5; placeRanktrackScreen(Ranktrack.PRIVATEER, (event.whoClicked as CraftPlayer))
			}
			36 -> if (pageNumber < 1) {
				pageNumber++; placeRanktrackScreen(Ranktrack.REFUGEE, (event.whoClicked as CraftPlayer))
			}
			38 -> if (pageNumber < 1) {
				pageNumber = 1; placeRanktrackScreen(Ranktrack.REFUGEE, (event.whoClicked as CraftPlayer))
			}
			45 -> if (pageNumber == 1) {
				placeGui(); pageNumber--
			} //switching pages
			else if (pageNumber > 1) {
				pageNumber--; placeRanktrackScreen(currentRanktrack, (event.whoClicked as CraftPlayer))
			} else return
			53 -> if (pageNumber < currentRanktrack.ranks.size) {
				pageNumber++; placeRanktrackScreen(currentRanktrack, (event.whoClicked as CraftPlayer))
			} else return
			else -> return
		}

	}

	private fun placeGui(player: CraftPlayer? = null) {
		inventory.clear()
		player?.handle?.connection?.send(
			ClientboundOpenScreenPacket(
				player.handle.containerMenu.containerId,
				player.handle.containerMenu.type,
				PaperAdventure.asVanilla(buildPageText(targetName))
			)
		)
		val blackBorderGlassPanes: Array<Int> =
			arrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 45, 46, 47, 48, 49, 50, 51, 52, 53)
		blackBorderGlassPanes.forEach { inventory.setItem(it, ItemStack(Material.BLACK_STAINED_GLASS_PANE)) }
		val refugeeIcon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		val industrialistIcon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		val outlawIcon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		val privateerIcon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		outlawIcon.editMeta {
			it.setCustomModelData(518)
			it.displayName(MiniMessage.miniMessage().deserialize("<red>${Ranktrack.OUTLAW.displayName}<red>"))
		}
		privateerIcon.editMeta {
			it.setCustomModelData(517)
			it.displayName(MiniMessage.miniMessage().deserialize("<blue>${Ranktrack.PRIVATEER.displayName}<blue>"))
		}
		industrialistIcon.editMeta {
			it.setCustomModelData(400)
			it.displayName(
				MiniMessage.miniMessage().deserialize("<gold>${Ranktrack.INDUSTRIALIST.displayName}<gold>")
			)
		}
		refugeeIcon.editMeta {
			it.setCustomModelData(400)
			it.displayName(MiniMessage.miniMessage().deserialize("<gray>${Ranktrack.REFUGEE.displayName}<gray>"))
		}
		inventory.setItem(9, outlawIcon)
		inventory.setItem(10, ItemStack(Material.RED_STAINED_GLASS_PANE))
		inventory.setItem(17, ItemStack(Material.RED_STAINED_GLASS_PANE))
		inventory.setItem(18, industrialistIcon)
		inventory.setItem(19, ItemStack(Material.ORANGE_STAINED_GLASS_PANE))
		inventory.setItem(26, ItemStack(Material.ORANGE_STAINED_GLASS_PANE))
		inventory.setItem(27, privateerIcon)
		inventory.setItem(28, ItemStack(Material.BLUE_STAINED_GLASS_PANE))
		inventory.setItem(35, ItemStack(Material.BLUE_STAINED_GLASS_PANE))
		inventory.setItem(36, refugeeIcon)
		inventory.setItem(37, ItemStack(Material.WHITE_STAINED_GLASS_PANE))
		inventory.setItem(44, ItemStack(Material.WHITE_STAINED_GLASS_PANE))
		for (outlawRanktrack in Ranktrack.OUTLAW.ranks) {
			val index = outlawRanktrack.levelPriority + 10
			val icon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
			icon.editMeta {
				it.setCustomModelData(outlawRanktrack.icon); it.displayName(
				MiniMessage.miniMessage()
					.deserialize("${outlawRanktrack.colour}${outlawRanktrack.displayName}${outlawRanktrack.colour}")
			)
			}
			inventory.setItem(index, icon)
		}
		for (industrialistRanktrack in Ranktrack.INDUSTRIALIST.ranks) {
			val index = industrialistRanktrack.levelPriority + 19
			val icon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
			icon.editMeta {
				it.setCustomModelData(industrialistRanktrack.icon); it.displayName(
				MiniMessage.miniMessage()
					.deserialize("${industrialistRanktrack.colour}${industrialistRanktrack.displayName}${industrialistRanktrack.colour}")
			)
			}
			inventory.setItem(index, icon)
		}
		for (privateerRanktrack in Ranktrack.PRIVATEER.ranks) {
			val index = privateerRanktrack.levelPriority + 28
			val icon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
			icon.editMeta {
				it.setCustomModelData(privateerRanktrack.icon); it.displayName(
				MiniMessage.miniMessage()
					.deserialize("${privateerRanktrack.colour}${privateerRanktrack.displayName}${privateerRanktrack.colour}")
			)
			}
			inventory.setItem(index, icon)
		}
		for (refugeeRanktrack in Ranktrack.REFUGEE.ranks) {
			val index = refugeeRanktrack.levelPriority + 38
			val icon = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
			icon.editMeta {
				it.setCustomModelData(refugeeRanktrack.icon); it.displayName(
				MiniMessage.miniMessage()
					.deserialize("${refugeeRanktrack.colour}${refugeeRanktrack.displayName}${refugeeRanktrack.colour}")
			)
			}
			inventory.setItem(index, icon)
		}
		for (i in 0..53) {
			if (inventory.getItem(i) == null) inventory.setItem(i, ItemStack(Material.BARRIER))
		}
	}

	private fun placeRanktrackScreen(ranktrack: Ranktrack, player: CraftPlayer) {
		inventory.clear()
		currentRanktrack = ranktrack
		player.handle.connection.send(
			ClientboundOpenScreenPacket(
				player.handle.containerMenu.containerId,
				player.handle.containerMenu.type,
				PaperAdventure.asVanilla(buildPageText(targetName))
			)
		)
		val mapOfRanks: MutableMap<Int, Ranktrack.Rank> = mutableMapOf(1000 to Ranktrack.Refugee.REFUGEE)
		mapOfRanks.clear()
		ranktrack.ranks.forEach {
			mapOfRanks[it.levelPriority] = it
		}
		val currentDisplayRanktrack: Ranktrack.Rank? = mapOfRanks.toList().find { it.first == (pageNumber) }?.second
		val ranktrackitem = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
		ranktrackitem.itemMeta.displayName(
			MiniMessage.miniMessage()
				.deserialize("${currentDisplayRanktrack?.colour} ${currentDisplayRanktrack?.displayName} ${currentDisplayRanktrack?.colour}")
		)
		ranktrackitem.editMeta {
			it.displayName(
				MiniMessage.miniMessage()
					.deserialize("${currentDisplayRanktrack?.colour} ${currentDisplayRanktrack?.displayName} ${currentDisplayRanktrack?.colour}")
			)
			it.lore(mutableListOf(MiniMessage.miniMessage().deserialize("<gray>${currentRanktrack.description}<gray>")))
			it.setCustomModelData(currentDisplayRanktrack?.icon)
		}
		inventory.setItem(4, ranktrackitem)
		/**
		 * This is the border glass and gray filler generator
		 */

		val borderBlackGlasspaneLocations: Array<Int> = arrayOf(9, 18, 27, 36, 45, 8, 17, 26, 35, 36, 44, 53)
		borderBlackGlasspaneLocations.forEach { inventory.setItem(it, ItemStack(Material.BLACK_STAINED_GLASS_PANE)) }
		val grayFillerGlassPaneLocations: Array<Int> = arrayOf(
			10,
			11,
			12,
			13,
			14,
			15,
			16,
			19,
			20,
			21,
			22,
			23,
			24,
			25,
			28,
			29,
			30,
			31,
			32,
			33,
			34,
			37,
			38,
			39,
			40,
			41,
			42,
			42,
			43,
			46,
			47,
			48,
			49,
			50,
			51,
			52
		)
		grayFillerGlassPaneLocations.forEach {
			if (inventory.getItem(it) == null) (inventory.setItem(
				it,
				ItemStack(Material.GRAY_STAINED_GLASS_PANE)
			))
		}
		/**
		 * This is just to add in the top filler glass panes, green for *has the ranktrack or has surpassed it
		 *  red for *Doesn't have that ranktrack yes
		 *  orange for *not their ranktrack tree
		 */
		if (currentDisplayRanktrack != null) {
			if (ranktrack.ranks.contains(targetsRanktrack) && (currentDisplayRanktrack.levelPriority <= targetsRanktrack.levelPriority)) {
				val greenFillerGlassPaneLocations: Array<Int> = arrayOf(1, 2, 3, 5, 6, 7)
				greenFillerGlassPaneLocations.forEach {
					inventory.setItem(
						it,
						ItemStack(Material.GREEN_STAINED_GLASS_PANE)
					)
				}
			} else if (ranktrack.ranks.contains(targetsRanktrack) && (currentDisplayRanktrack.levelPriority >= targetsRanktrack.levelPriority)) {
				val redFillerGlassPaneLocations: Array<Int> = arrayOf(1, 2, 3, 5, 6, 7)
				redFillerGlassPaneLocations.forEach {
					inventory.setItem(
						it,
						ItemStack(Material.RED_STAINED_GLASS_PANE)
					)
				}
			} else {
				val orangeFillerGlassPaneLocations: Array<Int> = arrayOf(1, 2, 3, 5, 6, 7)
				orangeFillerGlassPaneLocations.forEach {
					inventory.setItem(
						it,
						ItemStack(Material.ORANGE_STAINED_GLASS_PANE)
					)
				}
			}

			/**
			 * Arrows for switching between the ranktrack.ranks
			 */
			if (pageNumber >= 1) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(105)
				}

				inventory.setItem(45, leftArrow)
				val exitButton = ItemStack(Material.BARRIER)
				exitButton.editMeta {
					it.displayName(MiniMessage.miniMessage().deserialize("<red>Exit to Menu<red>"))
				}
				inventory.setItem(0, exitButton)
			}

			if (pageNumber < currentRanktrack.ranks.size) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(103)
				}

				inventory.setItem(53, leftArrow)
			}
		}
	}

	companion object {
		private val String.minecraftLength: Int
			get() =
				this.sumOf {
					@Suppress("Useless_Cast")
					when (it) {
						'i', '!', ',', '.', '\'', ':' -> 2
						'l' -> 3
						'I', 't', ' ' -> 4
						'k', 'f' -> 5
						else -> 6
					} as Int
				}

		private val Int.resetCode: Char get() = (0xDFFF + this).toChar()

		private fun buildPageText(
			target: String
		): TextComponent {
			val header = "$target's Ranktrack info"
			val string =
				"<white><font:horizonsend:special>\uE007\uF8FF\uE0A8<reset>$header<font:horizonsend:special>${(header.minecraftLength - 21).resetCode}"
			return MiniMessage.miniMessage().deserialize(string) as TextComponent
		}
	}
}