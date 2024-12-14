package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput

import net.horizonsend.ion.common.utils.text.ANVIL_BACKGROUND
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.blankItem
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.InputValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.applyDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.applyLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.inventoryaccess.component.AdventureComponentWrapper
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.AnvilWindow
import java.util.function.Consumer

class TextInputMenu(
	val player: Player,
	val title: Component,
	val description: Component,
	val backButtonHandler: ((Player) -> Unit)?,
	val inputValidator: InputValidator,
	val successfulInputHandler: Consumer<String>
) {
	var currentInput = ""

	fun createGui(): Gui {
		val gui = Gui.normal()

		gui.setStructure(". v x")

		gui.addIngredient('.', blankItem)
			.addIngredient('x', confirmButton)

		if (backButtonHandler != null) {
			gui.addIngredient('v', backButton)
		}

		return gui.build()
	}

	fun open() {
		val gui = createGui()

		val text = GuiText("")
			.addBackground(GuiText.GuiBackground(
				backgroundChar = ANVIL_BACKGROUND,
				horizontalShift = -52
			))
			.addBackground(GuiText.GuiBackground(
				backgroundChar = BACKGROUND_EXTENDER,
				verticalShift = -11
			))
			.add(title, line = -2, verticalShift = -3)
			.add(description, line = -1, verticalShift = -2)
			.build()

		val window = AnvilWindow.single()
			.setViewer(player)
			.setTitle(AdventureComponentWrapper(text))
			.setGui(gui)
			.addRenameHandler { string ->
				currentInput = string
				confirmButton.notifyWindows()
			}
			.build()

		window.open()
	}

	private val backButton = createButton(
		ItemStack(Material.BARRIER).applyDisplayName(text("Go Back", WHITE))
	) { _, player, _ ->
		player.closeInventory()
		backButtonHandler?.invoke(player)
	}

	private val confirmButton = object : AbstractItem() {
		private fun getSuccessState(result: ValidatorResult.Success): ItemStack {
			val base = GuiItem.CHECKMARK.makeItem().applyDisplayName(text("Confirm", GREEN))

			if (result is ValidatorResult.ResultsResult) {
				val more = result.results.size > 5

				return base.applyLore(result.results.take(5).plus(
					if (more) {
						template(text("{0} more results", WHITE), bracketed(text(result.results.size, AQUA)))
					} else empty()
				))
			}

			return base
		}

		private fun getFailureState(result: ValidatorResult.FailureResult) = ItemStack(Material.BARRIER)
			.applyDisplayName(text("Invalid Input!", RED))
			.applyLore(listOf(result.message))

		override fun getItemProvider(): ItemProvider {
			val result = inputValidator.isValid(currentInput)

			return ItemProvider {
				when (result) {
					is ValidatorResult.Success -> getSuccessState(result)
					is ValidatorResult.FailureResult -> getFailureState(result)
				}
			}
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val result = inputValidator.isValid(currentInput)
			if (!result.success) return notifyWindows()
			successfulInputHandler.accept(currentInput)
		}
	}

	companion object {
		fun Player.anvilInputText(
			prompt: Component,
			description: Component = empty(),
			backButtonHandler: ((Player) -> Unit)? = null,
			inputValidator: InputValidator,
			handler: Consumer<String>
		) {
			TextInputMenu(
				player = this,
				title = prompt,
				description = description,
				backButtonHandler = backButtonHandler,
				inputValidator = inputValidator,
				successfulInputHandler = handler
			).open()
		}
	}
}
