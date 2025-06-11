package net.horizonsend.ion.server.gui.invui.economy

import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.economy.misc.StationRentalAreas
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalArea
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.misc.util.ConfirmationMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.TextColor
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.window.Window
import java.time.Duration

class RentalAreaManageMenu(viewer: Player, val region: RegionRentalArea) : InvUIWindowWrapper(viewer, async = true) {
	var refreshTask: UpdateTask? = null

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				"w w w w w . . . .",
				"o . . . . . . . .",
				"a a a d d d m m m",
				"a a a d d d m m m",
			)
			.addIngredient('o', highlightButton)
			.addIngredient('a', abandonButton)
			.addIngredient('d', depositButton)
			.addIngredient('m', manageButton)
			.addIngredient('w', getWarningButton())
			.build()

		val window = Window.single()
			.setViewer(viewer)
			.setGui(gui)
			.setTitle(buildTitle())
			.addCloseHandler { cancelUpdateClass() }
			.build()

		scheduleUpdateTask()

		return window
	}

	override fun buildTitle(): Component {
		val (days, hours, minutes, seconds) = getDurationBreakdown(StationRentalAreas.getTimeUntilCollection().toMillis())

		val mainText = GuiText("")
			.add(text("Area Management"), line = -1, alignment = GuiText.TextAlignment.CENTER, verticalShift = -4)
			.setSlotOverlay(
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
				"# # # # # # # # #",
			)
			.setGuiIconOverlay(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". a . . b . . c .",
				". . . . . . . . .",
			)
			.addIcon('a', GuiIcon.bazaarBuyOrder(RED))
			.addIcon('b', GuiIcon.bazaarSellOrder(NamedTextColor.BLUE))
			.addIcon('c', GuiIcon.bazaarBuyOrder(NamedTextColor.YELLOW))
			.add(ofChildren(text("Rent: "), getMenuTitleName(region.rent.toCreditComponent())), line = 0)
			.add(ofChildren(text("Balance: "), getMenuTitleName(region.rentBalance.toCreditComponent())), line = 1)
			.build()

		val warningText = GuiText("", guiWidth = DEFAULT_GUI_WIDTH / 2)

		if (region.rentBalance < 0) {
			warningText.add(ofChildren(text("=« ", HE_DARK_GRAY), text("WARNING", TextColor.color(255, 0 , 0)), text(" »=", HE_DARK_GRAY),), line = 2, alignment = GuiText.TextAlignment.CENTER)
			warningText.add(text("Deposit Now!", RED), line = 3, alignment = GuiText.TextAlignment.CENTER)
		}
		else if (region.rentBalance < region.rent) {
			warningText.add(ofChildren(text("=« ", HE_DARK_GRAY), text("WARNING", TextColor.color(255, 0 , 0)), text(" »=", HE_DARK_GRAY),), line = 2, alignment = GuiText.TextAlignment.CENTER)
			warningText.add(text("Low Balance!", RED), line = 3, alignment = GuiText.TextAlignment.CENTER)
		}

		val durationText = GuiText("", guiWidth = DEFAULT_GUI_WIDTH / 2)
			.add(text("Next Collection:"), line = 0, horizontalShift = DEFAULT_GUI_WIDTH / 2)
			.add(ofChildren(getMenuTitleName(text(days, WHITE)), text(" Days,")), line = 1, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 3)
			.add(ofChildren(getMenuTitleName(text(hours, WHITE)), text(" Hours,")), line = 2, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 3)
			.add(ofChildren(getMenuTitleName(text(minutes, WHITE)), text(" Minutes,")), line = 3, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 3)
			.add(ofChildren(getMenuTitleName(text(seconds, WHITE)), text(" Seconds")), line = 4, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 3)
			.build()

		return ofChildren(mainText, durationText, warningText.build())
	}

	private fun scheduleUpdateTask() {
		refreshTask = UpdateTask()
		refreshTask?.runTaskTimerAsynchronously(IonServer, 20L, 20L)
	}

	private fun cancelUpdateClass() {
		refreshTask?.cancel()
	}

	inner class UpdateTask : BukkitRunnable() {
		override fun run() {
			val openWindow = getOpenWindow()

			if (openWindow == null || !openWindow.isOpen) {
				cancel()
				return
			}

			refreshTitle()
		}

		override fun cancel() {
			refreshTask = null
		}
	}

	private val highlightButton = GuiItem.OUTLINE.makeItem(text("Outline Area"))
		.updateLore(listOf(text("Highlight rental area boundaries for 30 seconds.", HE_MEDIUM_GRAY)))
		.makeGuiButton { _, _ -> StationRentalAreas.highlightBoundaries(viewer, region, Duration.ofSeconds(30L)) }

	private val abandonButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Abandon Area")), fallbackLoreProvider = {
		listOf(
			text("Give up your claim on this area. You will lose access", HE_MEDIUM_GRAY),
			text("to the area inside, and it will be made available to  other players.", HE_MEDIUM_GRAY),
			text("The remaining balance will be refunded to you.", HE_MEDIUM_GRAY),
		)
	}) { _, _ -> ConfirmationMenu.promptConfirmation(this, GuiText("Confirm Claim Abandonment")) { abandon(this) } }

	private fun abandon(confimButton: FeedbackLike) {
		val async = StationRentalAreas.abandon(viewer, region)
		async.withResult {
			if (it.isSuccess())  {
				it.sendReason(viewer)
				Tasks.sync {
					viewer.closeInventory()
				}
			} else {
				confimButton.updateWith(it)
				abandonButton.updateWith(async)
			}
		}
	}

	private val depositButton = GuiItem.EMPTY.makeItem(text("Deposit Rent"))
		.updateLore(listOf(text("Highlight rental area boundaries for 30 seconds.", HE_MEDIUM_GRAY)))
		.makeGuiButton { _, _ -> }

	private val manageButton = GuiItem.EMPTY.makeItem(text("Manage Area"))
		.updateLore(listOf(text("Highlight rental area boundaries for 30 seconds.", HE_MEDIUM_GRAY)))
		.makeGuiButton { _, _ -> }

	private fun getWarningButton() = ItemProvider {
		if (region.rentBalance < 0) {
			GuiItem.EMPTY.makeItem(text("Warning: Negative Balance!")).updateLore(listOf(
				text("The rent has not been paid for one week!", HE_MEDIUM_GRAY),
				text("If the balance remains negative at the time of next rent collection, you will lose access", HE_MEDIUM_GRAY),
				text("to this claim, and whatever is stored inside it.", HE_MEDIUM_GRAY)
			))
		}
		else if (region.rentBalance < region.rent) {
			GuiItem.EMPTY.makeItem(text("Warning: Low Balance!")).updateLore(listOf(
				text("There are not enough credits to pay for the next rent collection!", HE_MEDIUM_GRAY),
				text("If the balance remains negative for another week, you will lose access", HE_MEDIUM_GRAY),
				text("to this claim, and whatever is stored inside it.", HE_MEDIUM_GRAY)
			))
		}
		else GuiItem.EMPTY.makeItem(text(""))
	}.makeGuiButton { _, _ ->  }.tracked()
}

