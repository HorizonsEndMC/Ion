package net.horizonsend.ion.server.features.achievements

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.CHETHERITE_CHARACTER
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.screens.GuiText
import net.horizonsend.ion.server.features.screens.TextScreen
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.minecraft.network.protocol.game.ClientboundOpenScreenPacket
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import kotlin.math.ceil
import kotlin.math.min

class AchievementsScreen private constructor(
	private val targetName: String,
	private val targetAchievements: List<Achievement>
) : TextScreen(buildPageText(targetName, 0, targetAchievements)) {

	private var pageNumber: Int = 0

	constructor(targetName: String) : this(targetName, SLPlayer[targetName]?.achievements?.map { Achievement.valueOf(it) }?.toList() ?: listOf())

	init {
		placeAchievementIcons()
	}

	override fun handleInventoryClick(event: InventoryClickEvent) {
		when (event.slot) {
			BOTTOM_LEFT_CORNER_INVENTORY_INDEX -> if (pageNumber > 0) pageNumber-- else return
			BOTTOM_RIGHT_CORNER_INVENTORY_INDEX -> if (pageNumber < MAX_PAGES) pageNumber++ else return
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
		// Five achievements can be displayed per page. Get the first achievement index depending on the page
		val startIndex = pageNumber * ACHIEVEMENTS_PER_PAGE

		// "..<" is the syntax for an open-ended range (start index included, end index excluded)
		for (achievementIndex in startIndex until startIndex + ACHIEVEMENTS_PER_PAGE) {
			val achievement = try {
				// Attempt to get the index of the achievement
				Achievement.entries[achievementIndex]
			} catch (_: Exception) {
				// No achievement found
				inventory.setItem((achievementIndex - startIndex) * INVENTORY_SLOTS_PER_ROW, null)
				continue
			}

			val item: ItemStack

			if (achievement.icon != null) {
				item = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				item.editMeta {
					it.setCustomModelData(achievement.icon)
				}
			} else {
				// Use vanilla items for certain achievements
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

			inventory.setItem((achievementIndex - startIndex) * INVENTORY_SLOTS_PER_ROW, item)
		}

		// Set left/right arrows
		if (pageNumber > 0) {
			if (inventory.getItem(BOTTOM_LEFT_CORNER_INVENTORY_INDEX) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(UI_LEFT)
				}

				inventory.setItem(BOTTOM_LEFT_CORNER_INVENTORY_INDEX, leftArrow)
			}
		} else {
			inventory.setItem(BOTTOM_LEFT_CORNER_INVENTORY_INDEX, null)
		}

		if (pageNumber < MAX_PAGES) {
			if (inventory.getItem(BOTTOM_RIGHT_CORNER_INVENTORY_INDEX) == null) {
				val leftArrow = ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				leftArrow.editMeta {
					it.displayName(Component.empty())
					it.setCustomModelData(UI_RIGHT)
				}

				inventory.setItem(BOTTOM_RIGHT_CORNER_INVENTORY_INDEX, leftArrow)
			}
		} else {
			inventory.setItem(BOTTOM_RIGHT_CORNER_INVENTORY_INDEX, null)
		}
	}

	companion object {
		private const val MAX_PAGES = 8
		private const val INVENTORY_SLOTS_PER_ROW = 9
		private const val ACHIEVEMENTS_PER_PAGE = 5
		private const val BOTTOM_LEFT_CORNER_INVENTORY_INDEX = 45
		private const val BOTTOM_RIGHT_CORNER_INVENTORY_INDEX = 53
		private const val UI_LEFT = 105
		private const val UI_RIGHT = 103
		private const val PAGE_NUMBER_VERTICAL_SHIFT = 4

		private fun buildPageText(
			target: String,
			pageNumber: Int,
			targetAchievements: List<Achievement>
		): TextComponent {

			// create a new GuiText builder
			val header = "$target's Achievements"
			val guiText = GuiText(header)

			// get the index of the first achievement to display for this page
			val startIndex = pageNumber * ACHIEVEMENTS_PER_PAGE

			for (achievementIndex in startIndex until min(startIndex + ACHIEVEMENTS_PER_PAGE, Achievement.entries.size)) {

				val achievement = Achievement.entries[achievementIndex]
				val hasAchievement = targetAchievements.contains(achievement)

				// each inventory slot occupies two lines; reserve two lines per achievement
				val line = (achievementIndex - startIndex) * 2
				val colour = if (hasAchievement) GREEN else RED

				// achievement title and XP
				guiText.add(text(achievement.title, colour), line, GuiText.TextAlignment.LEFT, horizontalShift = 21)
				guiText.add(text("${achievement.experienceReward}XP", BLUE), line, GuiText.TextAlignment.RIGHT)

				// achievement description and credit/chetherite reward
				guiText.add(
					text(achievement.description, DARK_GRAY),
					line = line + 1,
					GuiText.TextAlignment.LEFT,
					horizontalShift = 21
				)

				guiText.add(
					text("C${achievement.creditReward}", DARK_GREEN)
						// if this achievement rewards chetherite
						.append(if (achievement.chetheriteReward != 0) ofChildren(
							text(" ${achievement.chetheriteReward}", DARK_PURPLE),
							text("$CHETHERITE_CHARACTER", WHITE).font(SPECIAL_FONT_KEY)
						) else Component.empty()),
					line = line + 1,
					GuiText.TextAlignment.RIGHT
				)
			}

			// page number
			val pageNumberString = "${pageNumber + 1} / ${ceil(Achievement.entries.size / ACHIEVEMENTS_PER_PAGE.toDouble()).toInt()}"
			guiText.add(
				text(pageNumberString),
				line = 10,
				GuiText.TextAlignment.CENTER,
				verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
			)

			return guiText.build() as TextComponent
		}
	}
}
