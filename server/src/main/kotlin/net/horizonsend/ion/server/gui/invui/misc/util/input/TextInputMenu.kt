package net.horizonsend.ion.server.gui.invui.misc.util.input

import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.text.ANVIL_BACKGROUND
import net.horizonsend.ion.common.utils.text.BACKGROUND_EXTENDER
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.gui.GuiItem
import net.horizonsend.ion.server.features.gui.GuiItems
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.gui.item.AsyncItem.Companion.loadingItem
import net.horizonsend.ion.server.features.nations.gui.skullItem
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.CommonGuiWrapper
import net.horizonsend.ion.server.gui.invui.InvUIWindowWrapper
import net.horizonsend.ion.server.gui.invui.bazaar.REMOTE_WARINING
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.CollectionSearchValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.updateDisplayName
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.AQUA
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.event.inventory.InventoryClickEvent
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.Gui
import xyz.xenondevs.invui.item.ItemProvider
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.AnvilWindow
import xyz.xenondevs.invui.window.Window
import java.util.function.Consumer

class TextInputMenu<T : Any>(
    viewer: Player,
    val title: Component,
    val description: Component = empty(),
    val backButtonHandler: ((Player) -> Unit)?,
    val inputValidator: InputValidator<T>,
    val componentTransformer: (T) -> Component = { it.toComponent() },
    val successfulInputHandler: ConfirmationButton<T>.(ClickType, ValidatorResult.ValidatorSuccess<T>) -> Unit
) : InvUIWindowWrapper(viewer) {
	var currentInput = ""

	override fun buildWindow(): Window {
		val gui = Gui.normal()
			.setStructure(". v x")
			.addIngredient('.', GuiItems.blankItem)
			.addIngredient('v', backButton)
			.addIngredient('x', confirmButton)

		return AnvilWindow.single()
			.setViewer(viewer)
			.setTitle(buildTitle())
			.setGui(gui)
			.addRenameHandler { string ->
				currentInput = string
				confirmButton.update()
			}
			.build()
	}

	override fun buildTitle(): Component = GuiText("")
		.addBackground(
            GuiText.GuiBackground(
                backgroundChar = ANVIL_BACKGROUND,
                horizontalShift = -52
            )
        )
		.add(title, line = -2, verticalShift = -3)
		.add(description, line = -1, verticalShift = -2)
		.addBackground(
            GuiText.GuiBackground(
                backgroundChar = BACKGROUND_EXTENDER,
                verticalShift = -11
            )
        )
		.build()

	private val backButton = GuiItems.createButton(GuiItem.CANCEL.makeItem(text("Go Back"))) { _, player, _ ->
        player.closeInventory()
        backButtonHandler?.invoke(player)
    }

	/**
	 * Confirm button for text input menus.
	 * This is its own class because it's passed into the result consumer function as the reciever, for access to update
	 **/
	private val confirmButton = ConfirmationButton(this)

	class ConfirmationButton<T : Any>(val parent: TextInputMenu<T>) : AbstractItem() {
		private var loreOverride: List<Component>? = null

		fun updateLoreOverride(lore: List<Component>) {
			loreOverride = lore
		}

		private fun getSuccessState(result: ValidatorResult.ValidatorSuccess<T>): ItemStack {
			val base = GuiItem.CHECKMARK.makeItem().updateDisplayName(text("Confirm", NamedTextColor.GREEN))

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
                            	text("{0} more results", NamedTextColor.WHITE),
                            	bracketed(text(result.results.size - 5, AQUA))
                        	)
							else empty()
                        )
				)
			}

			return base
		}

		private fun getFailureState(result: ValidatorResult.FailureResult<T>): ItemStack {
			val base = ItemStack(Material.BARRIER)
				.updateDisplayName(text("Invalid Input!", NamedTextColor.RED))

			if (loreOverride != null) {
				val clone = loreOverride!!.toList()
				loreOverride = null

				base.updateLore(clone)
				return base
			}

			return base.updateLore(result.message)
		}

		private var provider = loadingItem
		private var loaded: Boolean = false

		override fun getItemProvider(): ItemProvider = provider

		override fun handleClick(clickType: ClickType, player: Player, event: InventoryClickEvent) {
			if (!loaded) return

			val result = parent.inputValidator.isValid(parent.currentInput)
			if (result !is ValidatorResult.ValidatorSuccess<T>) return notifyWindows()
			parent.successfulInputHandler.invoke(this, clickType, result)
			parent.refreshTitle()
		}

		fun update() {
			provider = loadingItem
			loaded = false
			notifyWindows()

			Tasks.async {
				val item = when (val result = parent.inputValidator.isValid(parent.currentInput)) {
					is ValidatorResult.ValidatorSuccess -> getSuccessState(result)
					is ValidatorResult.FailureResult -> getFailureState(result)
				}

				provider = ItemProvider { item }

				loaded = true
				Tasks.sync { notifyWindows() }
			}
		}
	}

	companion object {
		fun <T : Any> Player.openInputMenu(
			prompt: Component,
			description: Component = empty(),
			backButtonHandler: ((Player) -> Unit)? = null,
			componentTransformer: (T) -> Component = { it.toComponent() },
			inputValidator: InputValidator<T>,
			handler: ConfirmationButton<T>.(ClickType, ValidatorResult.ValidatorSuccess<T>) -> Unit
		) = TextInputMenu(viewer = this, title = prompt, description = description, backButtonHandler = backButtonHandler, inputValidator = inputValidator, componentTransformer = componentTransformer, successfulInputHandler = handler).openGui()

		fun <T : Any> Player.openSearchMenu(
            entries: Collection<T>,
            searchTermProvider: (T) -> Collection<String>,
            prompt: Component,
            description: Component = empty(),
            backButtonHandler: ((Player) -> Unit)? = null,
            componentTransformer: (T) -> Component = { it.toComponent() },
            itemTransformer: (T) -> ItemStack = { GuiItem.RIGHT.makeItem(it.toComponent()) },
            handler: CommonGuiWrapper.(ClickType, T) -> Unit,
		) = TextInputMenu(
			viewer = this,
            title = prompt,
            description = description,
            backButtonHandler = backButtonHandler,
            componentTransformer = componentTransformer,
            inputValidator = CollectionSearchValidator(entries, searchTermProvider),
            successfulInputHandler = confirmButton@{ type, success -> when (success) {
				// If it is a single entry result, we can skip right to it.
                is ValidatorResult.ValidatorSuccessSingleEntry<T> -> handler.invoke(this.parent, type, success.result)

				// If there are multiple resutls for the search, have the player select.
                is ValidatorResult.ValidatorSuccessMultiEntry<T> -> ItemMenu.selector(
                    title = GuiText("")
						.addBackground()
						.addBackground(GuiText.GuiBackground(
							backgroundChar = BACKGROUND_EXTENDER,
							verticalShift = -11
						))
						.add(text("Search Results For:"), line = -2, verticalShift = -4)
						.add(text("\"${parent.currentInput}\""), line = -1, verticalShift = -2)
						.build(),
                    player = this@openSearchMenu,
                    entries = success.results,
                    resultConsumer = handler,
                    itemTransformer = itemTransformer,
                    backButtonHandler = { this@confirmButton.parent.openGui() }
                )

                else -> throw NotImplementedError()
            } }
        ).openGui()

		fun searchSLPlayers(viewer: Player, entryConsumer: Consumer<SLPlayerId>) = Tasks.async {
			val players = SLPlayer.allIds()

			val nameCache = mutableMapOf<SLPlayerId, String>()

			fun getName(slPlayer: SLPlayerId): String {
				return nameCache.getOrPut(slPlayer) { SLPlayer.getName(slPlayer) ?: "UNKNOWN" }
			}

			Tasks.sync {
				viewer.openSearchMenu(
					entries = players.toList(),
					searchTermProvider = { listOfNotNull(getName(it)) },
					prompt = text("Enter Player Name"),
					description = empty(),
					componentTransformer = { text(getName(it)) },
					itemTransformer = { skullItem(it.uuid, getName(it)) },
					handler = { _, result -> entryConsumer.accept(result) }
				)
			}
		}

		fun searchTradeCities(
			viewer: Player,
			cities: List<TradeCityData>,
			backButtonHandler: ((Player) -> Unit)? = null,
			handler: CommonGuiWrapper.(ClickType, TradeCityData) -> Unit
		) = viewer.openSearchMenu(
			entries = cities,
			searchTermProvider = { cityData: TradeCityData -> listOf(cityData.displayName, cityData.type.name) },
			prompt = text("Search for Trade Cities"),
			backButtonHandler = backButtonHandler,
			componentTransformer = { city: TradeCityData -> text(city.displayName) },
			itemTransformer = { city: TradeCityData ->
				val planet = city.planetIcon.updateDisplayName(text(city.displayName))

				val listingCount = BazaarItem.count(BazaarItem::cityTerritory eq city.territoryId)
				val territoryRegion = Regions.get<RegionTerritory>(city.territoryId)

				val lore = listOf(
					ofChildren(
						text("Located at ", HE_MEDIUM_GRAY), text(territoryRegion.name, AQUA),
						text(" on ", HE_MEDIUM_GRAY), text(territoryRegion.world, AQUA), text(".", GRAY)
					),
					template(text("{0} item listing${if (listingCount != 1L) "s" else ""}.", HE_MEDIUM_GRAY), listingCount)
				)

				planet.updateLore(if (!territoryRegion.contains(viewer.location)) lore.plus(REMOTE_WARINING) else lore)
			},
			handler = handler
		)
	}
}
