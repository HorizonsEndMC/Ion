package net.horizonsend.ion.server.features.achievements

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.customGuiBackground
import net.horizonsend.ion.common.utils.text.customGuiHeader
import net.horizonsend.ion.common.utils.text.minecraftLength
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.shiftDown
import net.horizonsend.ion.common.utils.text.shiftToLeftOfComponent
import net.horizonsend.ion.common.utils.text.shiftToRightGuiEdge
import net.horizonsend.ion.common.utils.text.withLeftShift
import net.horizonsend.ion.common.utils.text.withRightShift
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
		private const val INVENTORY_SLOT_HEIGHT = 18
		private const val INITIAL_ACHIEVEMENT_TEXT_HEIGHT = 12
		private const val ACHIEVEMENT_TEXT_OFFSET = 21
		private const val NEXT_LINE_OFFSET = 9
		private const val PAGE_NUMBER_OFFSET = 59

		// Shift by -1 pixel (left)
		private const val SHIFT_CHARACTERS_START = '\uE000'
		// Shift by 139 pixels (right)
		private const val REWARD_OFFSET_CHARACTER = '\uE18A'
		// Shift by 139 pixels (left)
		private const val ACHIEVEMENT_RETURN_CHARACTER = '\uE08A'
		// Shift by 59 pixels (right)
		private const val PAGE_NUMBER_OFFSET_CHARACTER = '\uE13A'
		// Shift by 108 pixels down
		private const val PAGE_NUMBER_VERTICAL_OFFSET = 108
		// Chetherite character
		private const val CHETHERITE_CHARACTER = '\uF8FE'


		/**
		 * Gets the width (in pixels) of a string rendered in the default Minecraft font.
		 */
		/*
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
		 */

		/**
		 * Gets the character that will shift the given length back to the beginning index
		 * \uE000 is the start of the Unicode Private Use Block; the beginning of the codes after applying the font
		 * <font:horizonsend:special> will shift the text cursor back by some amount. Adding more chars to this
		 * function will increase the number of pixels to shift left.
		 * Start at \uDFFF to account for one pixel shift.
		 */
		private val Int.resetCode: Char get() = (SHIFT_CHARACTERS_START - 1 + this)

		private fun buildPageText(
			target: String,
			pageNumber: Int,
			targetAchievements: List<Achievement>
		): TextComponent {
			val componentList = mutableListOf<Component>()
			val header = "$target's Achievements"

			/* Character explanation:
			\uE007 = shift text cursor left by 8 pixels (to left edge of GUI)
			\uF8FF = displays "text_screen_back.png" in the resource pack
			\uE0A8 = shift text cursor left by 169 pixels (to initial start position)
			(text_screen_back.png is 176 pixels wide; additional pixel for spacing)

			(header.minecraftLength - 21).resetCode = shift text cursor left by the length of the header, plus 21 pixels
			This places the text cursor within the second column of inventory slots with some offset
			 */
			//var string =
				//"<white><font:horizonsend:special>\uE007\uF8FF\uE0A8<reset>$header<font:horizonsend:special>${(header.minecraftLength - ACHIEVEMENT_TEXT_OFFSET).resetCode}"
			componentList.add(customGuiBackground(0xF8FF.toChar()))
			componentList.add(customGuiHeader(header))

			val startIndex = pageNumber * ACHIEVEMENTS_PER_PAGE

			for (achievementIndex in startIndex until min(startIndex + ACHIEVEMENTS_PER_PAGE, Achievement.entries.size)) {
				val achievement = Achievement.entries[achievementIndex]
				val hasAchievement = targetAchievements.contains(achievement)

				// Calculate amount of pixels to vertically offset
				val y = (achievementIndex - startIndex) * INVENTORY_SLOT_HEIGHT + INITIAL_ACHIEVEMENT_TEXT_HEIGHT
				val colour = if (hasAchievement) GREEN else RED

				// Calculate the offset for the XP and credit reward texts. Find the length in pixels, then subtract
				// the length from the offset character to find the correct start position for the text
				//val experienceStringLength = "${achievement.experienceReward}XP".minecraftLength
				//val experienceOffsetCode = REWARD_OFFSET_CHARACTER - achievement.title.minecraftLength - experienceStringLength

				//val creditChetheriteStringLength = "C${achievement.creditReward}${
				// The kk is a hacky fix, it adds up to 10 meaning it matches the length of the icon
				// The chetherite "character" is 10 pixels wide, meaning "kk" will visually match the length	
				//if (achievement.chetheriteReward != 0) " ${achievement.chetheriteReward}kk" else ""}".minecraftLength
				//val creditChetheriteOffsetCode =
				//	REWARD_OFFSET_CHARACTER - achievement.description.minecraftLength - creditChetheriteStringLength

				componentList.add(
					text(achievement.title, colour).withRightShift(ACHIEVEMENT_TEXT_OFFSET).shiftDown(y).shiftToRightGuiEdge()
					.append(text("${achievement.experienceReward}XP", BLUE).withLeftShift().shiftToLeftOfComponent())
					.shiftToLeftOfComponent(ACHIEVEMENT_TEXT_OFFSET)
				)

				componentList.add(
					text(achievement.description, DARK_GRAY).shiftDown(y + NEXT_LINE_OFFSET).shiftToRightGuiEdge()
						.append(text("C${achievement.creditReward}", DARK_GREEN)
							.append(if (achievement.chetheriteReward != 0) ofChildren(
								text(" ${achievement.chetheriteReward}", DARK_PURPLE),
								text(" $CHETHERITE_CHARACTER", WHITE).font(SPECIAL_FONT_KEY)
						) else Component.empty()
							)
						)
						.shiftToLeftOfComponent(ACHIEVEMENT_TEXT_OFFSET)
				)
				/*
				// The resource pack has many different "fonts" that are the regular font but shifted down by some
				// pixels. Switch to the font that offsets the text down to the desired height
				string += "<font:horizonsend:y$y>"
				// Achievement title
				string += "<$colour>${achievement.title}</$colour>"
				// Get the correct character code to shift the text right, making the reward text appear right justified
				string += "<font:horizonsend:special>$experienceOffsetCode</font>"
				// Experience reward text
				string += "<blue>${achievement.experienceReward}XP</blue>"
				// All lines end at the same right offset, so return back to the original achievement offset
				string += "<font:horizonsend:special>$ACHIEVEMENT_RETURN_CHARACTER" // Reset to start
				// Shift down one line
				string += "<font:horizonsend:y${y + NEXT_LINE_OFFSET}>"
				// Achievement description
				string += achievement.description
				// Add spacing for chetherite/credit reward text
				string += "<font:horizonsend:special>$creditChetheriteOffsetCode</font>"
				// Chetherite/credit reward
				string += "<dark_green>C${achievement.creditReward}</dark_green>${
					if (achievement.chetheriteReward != 0) { 
						" <dark_purple>${achievement.chetheriteReward}</dark_purple>" +
						"<white><font:horizonsend:special>$CHETHERITE_CHARACTER</font></white>" 
					} else ""
				}"
				// Return to achievement start offset
				string += "<font:horizonsend:special>$ACHIEVEMENT_RETURN_CHARACTER" // Reset to start
				 */
			}

			val pageNumberString = "${pageNumber + 1} / ${ceil(Achievement.entries.size / ACHIEVEMENTS_PER_PAGE.toDouble()).toInt()}"
			componentList.add(
				text(pageNumberString).shiftDown(PAGE_NUMBER_VERTICAL_OFFSET).withRightShift(
				PAGE_NUMBER_OFFSET - (pageNumberString.minecraftLength / 2)))

			// Shift the text right for the page number
			/*
			string += "<font:horizonsend:special>${(PAGE_NUMBER_OFFSET_CHARACTER - (pageNumberString.minecraftLength / 2))}"
			// Page number
			string += "<font:horizonsend:y$PAGE_NUMBER_VERTICAL_OFFSET>$pageNumberString"

			return MiniMessage.miniMessage().deserialize(string) as TextComponent
			 */
			return Component.textOfChildren(*componentList.toTypedArray())
		}
	}
}
