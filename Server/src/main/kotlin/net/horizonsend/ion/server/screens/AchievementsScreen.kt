package net.horizonsend.ion.server.screens

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.annotations.UpdateUnsafe
import kotlin.math.ceil
import kotlin.math.min
import net.horizonsend.ion.common.database.Achievement
import net.horizonsend.ion.common.database.PlayerData
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.minimessage.MiniMessage
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_19_R1.entity.CraftPlayer
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.jetbrains.exposed.sql.transactions.transaction

class AchievementsScreen private constructor(
	private val targetName: String,
	private val targetAchievements: List<Achievement>
) : TextScreen(buildPageText(targetName, 0, targetAchievements)) {
	private var pageNumber: Int = 0

	constructor(targetName: String) : this(targetName, transaction {
		PlayerData.getByUsername(targetName)?.achievements ?: listOf()
	})

	init {
		placeAchievementIcons()
	}

	@UpdateUnsafe
	override fun handleInventoryClick(event: InventoryClickEvent) {
		when (event.slot) {
			45 -> if (pageNumber > 0) pageNumber-- else return
			53 -> if (pageNumber < 8) pageNumber++ else return
			else -> return
		}

		// NMS trickery to update the Inventory name without re-opening it
		// See https://github.com/PaperMC/Paper/issues/7950 and https://github.com/PaperMC/Paper/pull/7979
		(event.whoClicked as CraftPlayer).handle.connection.send(
			ClientboundOpenScreenPacket(
				(event.whoClicked as CraftPlayer).handle.containerMenu.containerId,
				(event.whoClicked as CraftPlayer).handle.containerMenu.type,
				PaperAdventure.asVanilla(buildPageText(targetName, pageNumber, targetAchievements))
			)
		)

		placeAchievementIcons()
	}

	private fun placeAchievementIcons() {
		val startIndex = pageNumber * 5

		for (achievementIndex in startIndex .. startIndex + 4) {
			val achievement = try {
				Achievement.values()[achievementIndex]
			} catch (_: Exception) {
				inventory.setItem((achievementIndex - startIndex) * 9, null)
				continue
			}

			val item: ItemStack

			if (achievement.icon != null) {
				item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				item.editMeta {
					it.setCustomModelData(achievement.icon)
				}
			} else {
				val material = when (achievement) {
					Achievement.DETECT_MULTIBLOCK -> Material.NOTE_BLOCK
					Achievement.DETECT_SHIP -> Material.JUKEBOX
					Achievement.COMPLETE_CARGO_RUN -> Material.SHULKER_BOX
					else -> Material.BARRIER
				}

				item = ItemStack(material)
			}

			item.editMeta {
				it.displayName(Component.empty())
			}

			inventory.setItem((achievementIndex - startIndex) * 9, item)
		}

		if (pageNumber > 0) {
			if (inventory.getItem(45) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(105)
				}

				inventory.setItem(45, leftArrow)
			}
		} else inventory.setItem(45, null)

		if (pageNumber < 8) {
			if (inventory.getItem(53) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(103)
				}

				inventory.setItem(53, leftArrow)
			}
		} else inventory.setItem(53, null)
	}

	companion object {
		private val String.minecraftLength: Int get() =
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

		private fun buildPageText(target: String, pageNumber: Int, targetAchievements: List<Achievement>): TextComponent {
			val header = "$target's Achievements"
			var string = "<white><font:horizonsend:special>\uE007\uF8FF\uE0A8<reset>$header<font:horizonsend:special>${(header.minecraftLength - 21).resetCode}"

			val startIndex = pageNumber * 5

			for (achievementIndex in startIndex .. min(startIndex + 4, Achievement.values().size - 1)) {
				val achievement = Achievement.values()[achievementIndex]
				val hasAchievement = targetAchievements.contains(achievement)

				val y = (achievementIndex - startIndex) * 18 + 12
				val colour = if (hasAchievement) "green" else "red"

				val experienceStringLength = "${achievement.experienceReward}XP".minecraftLength
				val experienceOffsetCode = 0xE18A - achievement.title.minecraftLength - experienceStringLength

				val creditChetheriteStringLength = "C${achievement.creditReward}${
					// The kk is a hacky fix, it adds up to 10 meaning it matches the length of the icon
					if (achievement.chetheriteReward != 0) " ${achievement.chetheriteReward}kk" else ""
				}".minecraftLength
				val creditChetheriteOffsetCode = 0xE18A - achievement.description.minecraftLength - creditChetheriteStringLength

				string += "<font:horizonsend:y$y>" // Switch to line
				string += "<$colour>${achievement.title}</$colour>" // Achievement Name
				string += "<font:horizonsend:special>${experienceOffsetCode.toChar()}</font>" // Experience Offset
				string += "<blue>${achievement.experienceReward}XP</blue>" // Experience Amount
				string += "<font:horizonsend:special>\uE08A" // Reset to start
				string += "<font:horizonsend:y${y + 9}>" // Switch to line
				string += achievement.description // Achievement Description
				string += "<font:horizonsend:special>${creditChetheriteOffsetCode.toChar()}</font>" // Reward offset
				string += "<dark_green>C${achievement.creditReward}</dark_green>${
					if (achievement.chetheriteReward != 0)
						" <dark_purple>${achievement.chetheriteReward}</dark_purple><white><font:horizonsend:special>\uF8FE</font></white>"
					else
						""
				}"
				string += "<font:horizonsend:special>\uE08A" // Reset to start
			}

			val pageNumberString = "${pageNumber + 1} / ${ceil(Achievement.values().size / 5.0).toInt()}"

			string += "<font:horizonsend:special>${(0xE13A - (pageNumberString.minecraftLength / 2)).toChar()}"
			string += "<font:horizonsend:y106>$pageNumberString"

			return MiniMessage.miniMessage().deserialize(string) as TextComponent
		}
	}
}