package net.horizonsend.ion.server.features.progression.achievements

import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.utils.text.CHETHERITE_CHARACTER
import net.horizonsend.ion.common.utils.text.SPECIAL_FONT_KEY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry.CHETHERITE
import net.horizonsend.ion.server.features.gui.AbstractBackgroundPagedGui
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.get
import net.horizonsend.ion.server.miscellaneous.utils.setModel
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.vaultEconomy
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.BLUE
import net.kyori.adventure.text.format.NamedTextColor.DARK_GRAY
import net.kyori.adventure.text.format.NamedTextColor.DARK_GREEN
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.title.Title
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.addToSet
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.gui.structure.Markers
import xyz.xenondevs.invui.item.Item
import xyz.xenondevs.invui.item.builder.ItemBuilder
import xyz.xenondevs.invui.item.impl.SimpleItem
import xyz.xenondevs.invui.window.Window
import kotlin.math.ceil
import kotlin.math.min

class Achievements(val player: Player) : AbstractBackgroundPagedGui {

	companion object {
		private const val ACHIEVEMENTS_PER_PAGE = 5
		private const val PAGE_NUMBER_VERTICAL_SHIFT = 4
	}

	override var currentWindow: Window? = null

	override fun createGui(): PagedGui<Item> {
		val gui = PagedGui.items()
		val achievementIcons = mutableListOf<Item>()

		for (achievement in Achievement.entries) {
			// some achievements use a normal block instead of an achievement icon
			val itemStack = if (achievement.icon != null) ItemStack(Material.WARPED_FUNGUS_ON_A_STICK)
				.setModel(achievement.icon)
				.updateDisplayName(Component.empty())

			else ItemStack(when (achievement) {
				Achievement.DETECT_MULTIBLOCK -> Material.NOTE_BLOCK
				Achievement.DETECT_SHIP -> Material.JUKEBOX
				Achievement.COMPLETE_CARGO_RUN -> Material.SHULKER_BOX
				else -> Material.BARRIER
			})
			achievementIcons += SimpleItem(ItemBuilder(itemStack))
		}

		gui.setStructure(
			"x . . . . . . . .",
			"x . . . . . . . .",
			"x . . . . . . . .",
			"x . . . . . . . .",
			"x . . . . . . . .",
			"< . . . . . . . >")

		gui.addIngredient('x', Markers.CONTENT_LIST_SLOT_VERTICAL)
			.addIngredient('<', GuiItems.PageLeftItem())
			.addIngredient('>', GuiItems.PageRightItem())
			.setContent(achievementIcons)

		return gui.build()
	}

	override fun createText(player: Player, currentPage: Int): Component {

		val obtainedAchievements = SLPlayer[player].achievements.map { Achievement.valueOf(it) }.toList()

		// create a new GuiText builder
		val header = "${player.name}'s Achievements"
		val guiText = GuiText(header)
		guiText.addBackground()

		// get the index of the first achievement to display for this page
		val startIndex = currentPage * ACHIEVEMENTS_PER_PAGE

		for (achievementIndex in startIndex until min(startIndex + ACHIEVEMENTS_PER_PAGE, Achievement.entries.size)) {

			val achievement = Achievement.entries[achievementIndex]
			val hasAchievement = obtainedAchievements.contains(achievement)

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
		val pageNumberString = "${currentPage + 1} / ${ceil(Achievement.entries.size / ACHIEVEMENTS_PER_PAGE.toDouble()).toInt()}"
		guiText.add(
			text(pageNumberString),
			line = 10,
			GuiText.TextAlignment.CENTER,
			verticalShift = PAGE_NUMBER_VERTICAL_SHIFT
		)

		return guiText.build()
	}

	fun openMainWindow() {
		currentWindow = open(player).apply { open() }
	}
}

fun Player.rewardAchievement(achievement: Achievement, callback: () -> Unit = {}) = Tasks.async {
	if (!ConfigurationFiles.legacySettings().master) return@async

	val playerData = SLPlayer[this]
	if (playerData.achievements.map { Achievement.valueOf(it) }.find { it == achievement } != null) return@async

	SLPlayer.updateById(playerData._id, addToSet(SLPlayer::achievements, achievement.name))

	vaultEconomy?.depositPlayer(this@rewardAchievement, achievement.creditReward.toDouble())

	SLXP.addAsync(this@rewardAchievement, achievement.experienceReward, false)

	if (achievement.chetheriteReward > 0) {
		inventory.addItem(CHETHERITE.constructItemStack().asQuantity(achievement.chetheriteReward))
	}

	showTitle(
		Title.title(
			text(achievement.title).color(NamedTextColor.GOLD),
			text("Achievement Granted: ${achievement.description}").color(NamedTextColor.GRAY)
		)
	)

	sendRichMessage(
		"""
		<gold>${achievement.title}
		<gray>Achievement Granted: ${achievement.description}<reset>
		Credits: ${achievement.creditReward}
		Experience: ${achievement.experienceReward}
		""".trimIndent() + if (achievement.chetheriteReward != 0) "\nChetherite: ${achievement.chetheriteReward}" else ""
	)

	playSound(location, Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f)

	callback()
}
