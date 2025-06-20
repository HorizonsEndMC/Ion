package net.horizonsend.ion.server.gui.invui.economy

import net.horizonsend.ion.common.database.schema.economy.StationRentalZone
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.getDurationBreakdown
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_DARK_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.gui.icons.GuiIcon
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.economy.misc.StationRentalZones
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.settings.SettingsPageGui.Companion.createSettingsPage
import net.horizonsend.ion.server.features.gui.custom.settings.button.general.BooleanSupplierConsumerButton
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.types.RegionRentalZone
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.getMenuTitleName
import net.horizonsend.ion.server.gui.invui.misc.AccessManagementMenu
import net.horizonsend.ion.server.gui.invui.misc.util.ConfirmationMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.RangeDoubleValidator
import net.horizonsend.ion.server.gui.invui.utils.buttons.FeedbackLike
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getMoneyBalance
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
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

class RentalZoneHomeMenu(viewer: Player, val region: RegionRentalZone) : InvUIWindowWrapper(viewer, async = true) {
	var refreshTask: UpdateTask? = null

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				"w w w w w . . . .",
				"o . t . s . . . .",
				"a a a d d d m m m",
				"a a a d d d m m m",
			)
			.addIngredient('o', highlightButton)
			.addIngredient('t', transferButton)
			.addIngredient('s', settingsButton)
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
		val (days, hours, minutes, seconds) = getDurationBreakdown(StationRentalZones.getTimeUntilCollection().toMillis())
		val mainText = GuiText("")
			.add(text("Zone Management"), line = -1, alignment = GuiText.TextAlignment.CENTER, verticalShift = -4)
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
		else if (!StationRentalZones.canPayRent(region)) {
			warningText.add(ofChildren(text("=« ", HE_DARK_GRAY), text("WARNING", TextColor.color(255, 0 , 0)), text(" »=", HE_DARK_GRAY),), line = 2, alignment = GuiText.TextAlignment.CENTER)
			warningText.add(text("Low Balance!", RED), line = 3, alignment = GuiText.TextAlignment.CENTER)
		}

		val durationText = GuiText("", guiWidth = DEFAULT_GUI_WIDTH / 2)
			.add(text("Next Collection:"), line = 0, horizontalShift = DEFAULT_GUI_WIDTH / 2)
			.add(ofChildren(getMenuTitleName(text(days, WHITE)), text(" Days,")), line = 1, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 9)
			.add(ofChildren(getMenuTitleName(text(hours, WHITE)), text(" Hours,")), line = 2, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 9)
			.add(ofChildren(getMenuTitleName(text(minutes, WHITE)), text(" Minutes,")), line = 3, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 9)
			.add(ofChildren(getMenuTitleName(text(seconds, WHITE)), text(" Seconds")), line = 4, horizontalShift = (DEFAULT_GUI_WIDTH / 2) + 9)
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

	private val highlightButton = GuiItem.OUTLINE.makeItem(text("Outline Zone"))
		.updateLore(listOf(text("Highlight rental zone boundaries for 30 seconds.", HE_MEDIUM_GRAY)))
		.makeGuiButton { _, _ -> StationRentalZones.highlightBoundaries(viewer, region, Duration.ofSeconds(30L)) }

	private val abandonButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Abandon Zone")), fallbackLoreProvider = {
		listOf(
			text("Give up your claim on this zone. You will lose access", HE_MEDIUM_GRAY),
			text("to the zone inside, and it will be made available to  other players.", HE_MEDIUM_GRAY),
			text("The remaining balance will be refunded to you.", HE_MEDIUM_GRAY),
		)
	}) { _, _ -> ConfirmationMenu.promptConfirmation(this@RentalZoneHomeMenu, GuiText("Confirm Claim Abandonment")) { abandon(this) } }

	private fun abandon(confimButton: FeedbackLike) {
		val async = StationRentalZones.abandon(viewer, region)
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

	private val depositButton = FeedbackLike.withHandler(GuiItem.EMPTY.makeItem(text("Deposit Rent")), fallbackLoreProvider = {
		listOf(
			text(""),
			text(""),
			text("")
		)
	}) { _, _ -> deposit() }

	fun deposit() {
		viewer.openInputMenu(
			prompt = text("Enter Deposit Amount"),
			description = template(text("Between {0} and {1}"), getMenuTitleName(0.0.toCreditComponent()), getMenuTitleName(viewer.getMoneyBalance().toCreditComponent())),
			backButtonHandler = { openGui() },
			inputValidator = RangeDoubleValidator(0.0..viewer.getMoneyBalance()),
			handler = { _, result ->
				val depositAmount = result.result

				val depositResult = StationRentalZones.depositBalance(viewer, region, depositAmount)

				depositResult.withResult {
					depositButton.updateWith(it)

					refreshAll()
					openGui()
				}
			}
		)
	}

	private val manageButton = GuiItem.EMPTY.makeItem(text("Manage Access"))
		.updateLore(listOf(text("Manage who can access this menu.", HE_MEDIUM_GRAY)))
		.makeGuiButton { _, _ ->
			if (region.owner != viewer.slPlayerId) return@makeGuiButton

			AccessManagementMenu(
				viewer,
				region.id,
				StationRentalZone.Companion,
				StationRentalZone::trustedPlayers,
				StationRentalZone::trustedSettlements,
				StationRentalZone::trustedNations
			).openGui(this)
		}

	private fun getWarningButton() = ItemProvider {
		if (region.rentBalance < 0) {
			GuiItem.EMPTY.makeItem(text("Warning: Negative Balance!")).updateLore(listOf(
				text("The rent has not been paid for one week!", HE_MEDIUM_GRAY),
				text("If the balance remains negative at the time of next rent collection, you will lose access", HE_MEDIUM_GRAY),
				text("to this claim, and whatever is stored inside it.", HE_MEDIUM_GRAY)
			))
		}
		else if (!StationRentalZones.canPayRent(region)) {
			GuiItem.EMPTY.makeItem(text("Warning: Low Balance!")).updateLore(listOf(
				text("There are not enough credits to pay for the next rent collection!", HE_MEDIUM_GRAY),
				text("If the balance remains negative for another week, you will lose access", HE_MEDIUM_GRAY),
				text("to this claim, and whatever is stored inside it.", HE_MEDIUM_GRAY)
			))
		}
		else GuiItem.EMPTY.makeItem(text(""))
	}.makeGuiButton { _, _ ->  }.tracked()

	private val transferButton = GuiItem.RIGHT.makeItem(text("Transfer This Claim")).makeGuiButton { _, _ -> transfer() }
	private fun transfer() {
		Tasks.async {
			val players = SLPlayer.allIds().toList()

			Tasks.sync {
				viewer.openSearchMenu(
					entries = players,
					searchTermProvider = { listOfNotNull(SLPlayer.getName(it)) },
					prompt = text("Enter Player Name"),
					description = empty(),
					componentTransformer = { text(SLPlayer.getName(it) ?: "UNKNOWN") },
					itemTransformer = {
						val name = SLPlayer.getName(it)
						skullItem(it.uuid, name!!)
					},
					handler = { _, result ->
						StationRentalZones.transferOwnership(viewer, region, result)
					}
				)
			}
		}
	}

	private val settingsButton = GuiItem.GEAR.makeItem(text("Settings")).makeGuiButton { _, _ ->
		createSettingsPage(
			viewer,
			"Rental Zone Settings",
			BooleanSupplierConsumerButton(
				valueSupplier = region::collectRentFromOwnerBalance,
				valueConsumer = { region.setCollectRentFromBalance(it) },
				name = text("Collect Rent From Balance"),
				description = "If enabled, rent will be collected from the balance of the owner, ignoring the deposit on this claim, unless the balance is insufficient.",
				icon = GuiItem.LIST,
				defaultValue = false
			)
		).openGui(this)
	}
}

