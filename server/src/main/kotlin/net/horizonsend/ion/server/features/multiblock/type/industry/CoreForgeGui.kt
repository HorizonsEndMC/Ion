package net.horizonsend.ion.server.features.multiblock.type.industry

import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.text.ADVANCED_SHIP_FACTORY_CHARACTER
import net.horizonsend.ion.common.utils.text.DEFAULT_BACKGROUND_CHARACTER
import net.horizonsend.ion.common.utils.text.DEFAULT_GUI_WIDTH
import net.horizonsend.ion.common.utils.text.SPACE_RED_NEBULA_CHARACTER
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.BATTLECRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.CRUISER_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.LARGE_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MEDIUM_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.MINI_REACTOR_CORE
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys.SMALL_REACTOR_CORE
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem
import net.horizonsend.ion.server.features.gui.item.FeedbackItem
import net.horizonsend.ion.server.features.gui.item.ValueScrollButton
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.misc.BlueprintMenu
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.text.itemLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.entity.Player
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.window.Window

class CoreForgeGui(viewer: Player, val entity: CoreForgeEntity) : InvUIWindowWrapper(viewer, async = true) {
	private fun isValid(): Boolean = entity.isAlive
	var targetCore = entity.targetCore

	override fun buildWindow(): Window? {
		val gui = Gui.normal()
			.setStructure(
				". . . . . . . . .",
				". . . . . . . . .",
				". m a n . g o 4 .",
				". . . . . . . . .",
				". 6 7 . . . 1 . .",
				". . . . . . . . ."
			)
			.addIngredient('m', tracked { id ->
				AsyncItem({ MINI_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = MINI_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('a', tracked { id ->
				AsyncItem({ SMALL_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = SMALL_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('n', tracked { id ->
				AsyncItem({ MEDIUM_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = MEDIUM_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('g', tracked { id ->
				AsyncItem({ LARGE_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = LARGE_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('o', tracked { id ->
				AsyncItem({ CRUISER_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = CRUISER_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('4', tracked { id ->
				AsyncItem({ BATTLECRUISER_REACTOR_CORE.getValue().constructItemStack() }) {
					entity.targetCore = BATTLECRUISER_REACTOR_CORE.getValue().constructItemStack()
					refreshButtons(id)
					viewer.closeInventory()
					openGui(this)
				}
			})
			.addIngredient('6', GuiItems.CustomControlItem(text("Selected Core:"), GuiItem.RIGHT))
			.addIngredient('7', targetCore)
			.addIngredient('1', enableButton)
			.build()

		if (!isValid()) return null

		return normalWindow(gui)
	}

	override fun buildTitle(): Component {
		val text = GuiText(entity.guiTitle)
			.addBackground(
				GuiText.GuiBackground(
					backgroundChar = SPACE_RED_NEBULA_CHARACTER,
					backgroundWidth = DEFAULT_GUI_WIDTH,
					verticalShift = 0
				)
			)
		/*	.add(
				text("Core Forge").itemLore,
				verticalShift = -7 /* down 1 line */ + 2 /* Padding */,
				horizontalShift = DEFAULT_GUI_WIDTH + 2
			)*/
		return text.build()
	}

	val enableButton: FeedbackItem = FeedbackItem
		.builder({
			if (entity.isRunning) GuiItem.SHIP_FACTORY_RUNNING.makeItem(text("Start")) else GuiItem.CHECKMARK.makeItem(
				text("Start")
			)
		}) { _, player ->
			if (entity.userManager.currentlyUsed()) return@builder InputResult.FailureReason(
				listOf(
					text(
						"This core forge is already being used!",
						RED
					)
				)
			)
			val otherCheckResults = entity.checkEnableButton(player)

			val future = FutureInputResult()

			otherCheckResults.withResult { t ->
				if (!t.isSuccess()) {
					future.complete(t)
					return@withResult
				}

				future.complete(InputResult.SuccessReason(listOf(text("Enabled core forge.", GREEN))))
			}

			future
		}
		.withStaticFallbackLore(listOf(text("Start the core forge.")))
		.withSuccessHandler { _, player ->
			Tasks.async {
				entity.enable(player, this@CoreForgeGui)
			}
		}
		.build()
		.tracked()
}
