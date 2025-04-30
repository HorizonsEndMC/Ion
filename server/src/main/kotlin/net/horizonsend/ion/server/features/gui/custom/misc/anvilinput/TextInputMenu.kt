package net.horizonsend.ion.server.features.gui.custom.misc.anvilinput

import net.horizonsend.ion.common.utils.text.ANVIL_BACKGROUND
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems.blankItem
import net.horizonsend.ion.server.features.gui.GuiItems.createButton
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.custom.misc.ItemMenu
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.CollectionSearchValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.InputValidator
import net.horizonsend.ion.server.features.gui.custom.misc.anvilinput.validator.ValidatorResult
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
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
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.AnvilWindow

class TextInputMenu<T : Any>(
	val player: Player,
	val title: Component,
	val description: Component,
	val backButtonHandler: ((Player) -> Unit)?,
	val inputValidator: InputValidator<T>,
	val componentTransformer: (T) -> Component = { it.toComponent() },
	val successfulInputHandler: (ClickType, Pair<String, ValidatorResult.ValidatorSuccess<T>>) -> Unit
) {
	var currentInput = ""

	fun createGui(): Gui {
		val gui = Gui.normal()

		gui.setStructure(". v x")

		gui.addIngredient('.', blankItem)
			.addIngredient('v', backButton)
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
			.add(title, line = -2, verticalShift = -3)
			.add(description, line = -1, verticalShift = -2)
			.addBackground(GuiText.GuiBackground(
				backgroundChar = BACKGROUND_EXTENDER,
				verticalShift = -11
			))
			.build()

		val window = AnvilWindow.single()
			.setViewer(player)
			.setTitle(text)
			.setGui(gui)
			.addRenameHandler { string ->
				currentInput = string
				confirmButton.notifyWindows()
			}
			.build()

		window.open()
	}

	private val backButton = createButton(
		ItemStack(Material.BARRIER).updateDisplayName(text("Go Back", WHITE))
	) { _, player, _ ->
		player.closeInventory()
		backButtonHandler?.invoke(player)
	}

	private val confirmButton = object : AbstractItem() {
		private fun getSuccessState(result: ValidatorResult.ValidatorSuccess<T>): ItemStack {
			val base = GuiItem.CHECKMARK.makeItem().updateDisplayName(text("Confirm", GREEN))

			if (result is ValidatorResult.ValidatorSuccessMultiEntry<*>) {
				@Suppress("UNCHECKED_CAST")
				result as ValidatorResult.ValidatorSuccessMultiEntry<T>

				val more = result.results.size > 5

				return base.updateLore(
					result.results
						.take(5)
						.map { componentTransformer.invoke(it) }
						.plus(
							if (more) template(text("{0} more results", WHITE), bracketed(text(result.results.size - 5, AQUA))) else empty()
						)
				)
			}

			return base
		}

		private fun getFailureState(result: ValidatorResult.FailureResult<T>) = ItemStack(Material.BARRIER)
			.updateDisplayName(text("Invalid Input!", RED))
			.updateLore(listOf(result.message))

		override fun getItemProvider(): ItemProvider {
			val result = inputValidator.isValid(currentInput)

			return ItemProvider {
				when (result) {
					is ValidatorResult.ValidatorSuccess -> getSuccessState(result)
					is ValidatorResult.FailureResult -> getFailureState(result)
					else -> TODO("Not implemented")
				}
			}
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val result = inputValidator.isValid(currentInput)
			if (result !is ValidatorResult.ValidatorSuccess<T>) return notifyWindows()
			successfulInputHandler.invoke(clickType, currentInput to result)
		}
	}

	companion object {
		fun <T : Any> Player.anvilInputText(
			prompt: Component,
			description: Component = empty(),
			backButtonHandler: ((Player) -> Unit)? = null,
			componentTransformer: (T) -> Component = { it.toComponent() },
			inputValidator: InputValidator<T>,
			handler: (ClickType, Pair<String, ValidatorResult.ValidatorSuccess<T>>) -> Unit
		) {
			TextInputMenu(
				player = this,
				title = prompt,
				description = description,
				backButtonHandler = backButtonHandler,
				inputValidator = inputValidator,
				componentTransformer = componentTransformer,
				successfulInputHandler = handler
			).open()
		}

		fun <T : Any> Player.searchEntires(
			entries: Collection<T>,
			searchTermProvider: (T) -> Collection<String>,
			prompt: Component,
			description: Component = empty(),
			backButtonHandler: ((Player) -> Unit)? = null,
			componentTransformer: (T) -> Component = { it.toComponent() },
			itemTransformer: (T) -> ItemStack = { GuiItem.RIGHT.makeItem(it.toComponent()) },
			handler: (ClickType, T) -> Unit
		) {
			lateinit var textInput: TextInputMenu<T>

			textInput = TextInputMenu(
				player = this,
				title = prompt,
				description = description,
				backButtonHandler = backButtonHandler,
				componentTransformer = componentTransformer,
				inputValidator = CollectionSearchValidator(entries, searchTermProvider),
				successfulInputHandler = { type, (_, success) ->
					when (success) {
						is ValidatorResult.ValidatorSuccessSingleEntry<T> -> handler.invoke(type, success.result)

						is ValidatorResult.ValidatorSuccessMultiEntry<T> -> ItemMenu.selector(
							title = text("Ambiguous entry, select one."),
							player = this,
							entries = success.results,
							resultConsumer = handler,
							itemTransformer = itemTransformer,
							backButtonHandler = { textInput.open() }
						)

						else -> throw NotImplementedError()
					}
				}
			)

			textInput.open()
		}
	}
}
