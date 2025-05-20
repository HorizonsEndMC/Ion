package net.horizonsend.ion.server.gui.invui.input

import net.horizonsend.ion.common.utils.text.ANVIL_BACKGROUND
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.input.validator.CollectionSearchValidator
import net.horizonsend.ion.server.gui.invui.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.input.validator.ValidatorResult
import net.horizonsend.ion.server.gui.invui.utils.changeTitle
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import java.util.function.Supplier

class TextInputMenu<T : Any>(
	val player: Player,
	val titleSupplier: Supplier<Component>,
	val descriptionSupplier: Supplier<Component> = Supplier { Component.empty() },
	val backButtonHandler: ((Player) -> Unit)?,
	val inputValidator: InputValidator<T>,
	val componentTransformer: (T) -> Component = { it.toComponent() },
	val successfulInputHandler: ConfirmationButton<T>.(ClickType, Pair<String, ValidatorResult.ValidatorSuccess<T>>) -> Unit
) : CommonGuiWrapper {
	var currentInput = ""

	fun createGui(): Gui {
		val gui = Gui.normal()

		gui.setStructure(". v x")

		gui.addIngredient('.', GuiItems.blankItem)
			.addIngredient('v', backButton)
			.addIngredient('x', confirmButton)

		if (backButtonHandler != null) {
			gui.addIngredient('v', backButton)
		}

		return gui.build()
	}

	private var window: Window? = null

	override fun openGui() {
		val gui = createGui()

		val text = buildGuiText()

		val window = AnvilWindow.single()
			.setViewer(player)
			.setTitle(text)
			.setGui(gui)
			.addRenameHandler { string ->
				currentInput = string
				confirmButton.notifyWindows()
			}
			.build()

		this.window = window

		window.open()
	}

	fun refreshTitle() {
		window?.changeTitle(buildGuiText())
	}

	private fun buildGuiText(): Component = GuiText("")
		.addBackground(
            GuiText.GuiBackground(
                backgroundChar = ANVIL_BACKGROUND,
                horizontalShift = -52
            )
        )
		.add(titleSupplier.get(), line = -2, verticalShift = -3)
		.add(descriptionSupplier.get(), line = -1, verticalShift = -2)
		.addBackground(
            GuiText.GuiBackground(
                backgroundChar = BACKGROUND_EXTENDER,
                verticalShift = -11
            )
        )
		.build()

	private val backButton = GuiItems.createButton(GuiItem.CANCEL.makeItem(Component.text("Go Back"))) { _, player, _ ->
        player.closeInventory()
        backButtonHandler?.invoke(player)
    }

	private val confirmButton = ConfirmationButton(this)

	class ConfirmationButton<T : Any>(val parent: TextInputMenu<T>) : AbstractItem() {
		private var loreOverride: List<Component>? = null

		fun updateLoreOverride(lore: List<Component>) {
			loreOverride = lore
		}

		private fun getSuccessState(result: ValidatorResult.ValidatorSuccess<T>): ItemStack {
			val base = GuiItem.CHECKMARK.makeItem().updateDisplayName(Component.text("Confirm", NamedTextColor.GREEN))

			if (loreOverride != null) {
				val clone = loreOverride!!.toList()
				loreOverride = null
				return base.updateLore(clone)
			}

			if (result is ValidatorResult.ValidatorSuccessSingleEntry<*>) {
				result as ValidatorResult.ValidatorSuccessSingleEntry<T>

				return base.updateLore(listOf(parent.componentTransformer.invoke(result.result)))
			}

			if (result is ValidatorResult.ValidatorSuccessMultiEntry<*>) {
				result as ValidatorResult.ValidatorSuccessMultiEntry<T>

				val more = result.results.size > 5

				return base.updateLore(
					result.results
						.take(5)
						.map { parent.componentTransformer.invoke(it) }
						.plus(
							if (more) template(
                            	Component.text("{0} more results", NamedTextColor.WHITE),
                            	bracketed(Component.text(result.results.size - 5, NamedTextColor.AQUA))
                        	)
							else Component.empty()
                        )
				)
			}

			return base
		}

		private fun getFailureState(result: ValidatorResult.FailureResult<T>): ItemStack {
			val base = ItemStack(Material.BARRIER)
				.updateDisplayName(Component.text("Invalid Input!", NamedTextColor.RED))

			if (loreOverride != null) {
				val clone = loreOverride!!.toList()
				loreOverride = null

				base.updateLore(clone)
				return base
			}

			return base.updateLore(listOf(result.message))
		}

		override fun getItemProvider(): ItemProvider {
			val result = parent.inputValidator.isValid(parent.currentInput)

			return ItemProvider {
				when (result) {
					is ValidatorResult.ValidatorSuccess -> getSuccessState(result)
					is ValidatorResult.FailureResult -> getFailureState(result)
				}
			}
		}

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			val result = parent.inputValidator.isValid(parent.currentInput)
			if (result !is ValidatorResult.ValidatorSuccess<T>) return notifyWindows()
			parent.successfulInputHandler.invoke(this, clickType, parent.currentInput to result)
			parent.refreshTitle()
		}
	}

	companion object {
		fun <T : Any> Player.anvilInputText(
			prompt: Component,
			description: Component = Component.empty(),
			backButtonHandler: ((Player) -> Unit)? = null,
			componentTransformer: (T) -> Component = { it.toComponent() },
			inputValidator: InputValidator<T>,
			handler: ConfirmationButton<T>.(ClickType, Pair<String, ValidatorResult.ValidatorSuccess<T>>) -> Unit
		) = anvilInputText(
			prompt = { prompt },
			description = { description },
			backButtonHandler = backButtonHandler,
			componentTransformer = componentTransformer,
			inputValidator = inputValidator,
			handler = handler
		)

		fun <T : Any> Player.anvilInputText(
			prompt: Supplier<Component>,
			description: Supplier<Component> = Supplier { Component.empty() },
			backButtonHandler: ((Player) -> Unit)? = null,
			componentTransformer: (T) -> Component = { it.toComponent() },
			inputValidator: InputValidator<T>,
			handler: ConfirmationButton<T>.(ClickType, Pair<String, ValidatorResult.ValidatorSuccess<T>>) -> Unit,
		) {
			TextInputMenu(
                player = this,
                titleSupplier = prompt,
                descriptionSupplier = description,
                backButtonHandler = backButtonHandler,
                inputValidator = inputValidator,
                componentTransformer = componentTransformer,
                successfulInputHandler = handler
            ).openGui()
		}

		fun <T : Any> Player.searchEntires(
            entries: Collection<T>,
            searchTermProvider: (T) -> Collection<String>,
            prompt: Component,
            description: Component = Component.empty(),
            backButtonHandler: ((Player) -> Unit)? = null,
            componentTransformer: (T) -> Component = { it.toComponent() },
            itemTransformer: (T) -> ItemStack = { GuiItem.RIGHT.makeItem(it.toComponent()) },
            handler: (ClickType, T) -> Unit,
		): Unit = searchEntires(
			entries = entries,
			searchTermProvider = searchTermProvider,
			prompt = { prompt },
			description = { description },
			backButtonHandler = backButtonHandler,
			componentTransformer = componentTransformer,
			itemTransformer = itemTransformer,
			handler = handler
		)

		fun <T : Any> Player.searchEntires(
            entries: Collection<T>,
            searchTermProvider: (T) -> Collection<String>,
            prompt: Supplier<Component>,
            description: Supplier<Component> = Supplier { Component.empty() },
            backButtonHandler: ((Player) -> Unit)? = null,
            componentTransformer: (T) -> Component = { it.toComponent() },
            itemTransformer: (T) -> ItemStack = { GuiItem.RIGHT.makeItem(it.toComponent()) },
            handler: (ClickType, T) -> Unit
		) {
			lateinit var textInput: TextInputMenu<T>

			textInput = TextInputMenu(
                player = this,
                titleSupplier = prompt,
                descriptionSupplier = description,
                backButtonHandler = backButtonHandler,
                componentTransformer = componentTransformer,
                inputValidator = CollectionSearchValidator(entries, searchTermProvider),
                successfulInputHandler = { type, (search, success) ->
                    when (success) {
                        is ValidatorResult.ValidatorSuccessSingleEntry<T> -> handler.invoke(type, success.result)

                        is ValidatorResult.ValidatorSuccessMultiEntry<T> -> {
                            val extraLine = GuiText("")
                                .addBackground()
                                .addBackground(
                                    GuiText.GuiBackground(
                                        backgroundChar = BACKGROUND_EXTENDER,
                                        verticalShift = -11
                                    )
                                )
                                .add(Component.text("Search Results For:"), line = -2, verticalShift = -4)
                                .add(Component.text("\"$search\""), line = -1, verticalShift = -2)
                                .build()

                            ItemMenu.selector(
                                title = extraLine,
                                player = this@searchEntires,
                                entries = success.results,
                                resultConsumer = handler,
                                itemTransformer = itemTransformer,
                                backButtonHandler = { textInput.openGui() }
                            )
                        }

                        else -> throw NotImplementedError()
                    }
                }
            )

			textInput.openGui()
		}
	}
}
