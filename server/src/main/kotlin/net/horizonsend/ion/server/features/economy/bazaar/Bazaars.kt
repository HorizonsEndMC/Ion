package net.horizonsend.ion.server.features.economy.bazaar

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.mongodb.client.FindIterable
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.InputResult
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toComponent
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.economy.BazaarCommand
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.gui.custom.bazaar.BazaarPurchaseMenuGui
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.input.TextInputMenu.Companion.anvilInputText
import net.horizonsend.ion.server.gui.invui.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.empty
import net.kyori.adventure.text.Component.space
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.format.NamedTextColor.WHITE
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.ne
import java.util.function.Consumer
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

object Bazaars : IonServerComponent() {
	val strings = mutableListOf<String>()

	fun buildStrings() {
		strings.addAll(Material.entries.filter { it.isItem && !it.isLegacy && !it.isAir }.map { it.name })
		strings.addAll(CustomItemRegistry.identifiers)
		strings.addAll(MultiblockRegistration.getAllMultiblocks().map { "MULTIBLOCK_TOKEN[multiblock=${it.javaClass.simpleName}]" })
		strings.remove("MULTIBLOCK_TOKEN")
		strings.remove("PACKAGED_MULTIBLOCK")
	}

	override fun onEnable() {
		buildStrings()
		Tasks.asyncRepeat(20L, 20 * 60 * 60L, /* Every hour */ ::cleanExpiredBazaarEntries)
	}

    fun onClickBazaarNPC(player: Player, city: TradeCityData) {
		val territoryId: Oid<Territory> = city.territoryId

		openMainMenu(territoryId, player, false)
	}

	fun openMainMenu(territoryId: Oid<Territory>, player: Player, remote: Boolean): Unit = Tasks.async {
		MenuHelper.run {
			val backButton = guiButton(Material.IRON_DOOR) {
				Tasks.sync {
					BazaarCommand.onBrowse(player)
				}
			}.setName(text("Go Back to City Selection"))

			val searchButton = guiButton(Material.NAME_TAG) {
				Tasks.sync {
					player.anvilInputText(
						"Enter Item Name".toComponent(),
						backButtonHandler = { player ->
							Tasks.sync { openMainMenu(territoryId, player, remote) }
						},
						inputValidator = InputValidator { ValidatorResult.ValidatorSuccessEmpty(it) }
					) { _, (result, _) ->
						val searchBackButton = guiButton(Material.IRON_DOOR) {
							Tasks.sync {
								openMainMenu(territoryId, player, remote)
							}
						}.setName(text("Go Back to City"))

						Tasks.async {
							val items: List<GuiItem> = getGuiItems(territoryId, remote, search(territoryId, result))

							Tasks.sync {
								player.openPaginatedMenu("Search Query : $result", items, listOf(searchBackButton))
							}
						}
					}

				}
			}.setName(text("Search"))

			val titleButtons: List<GuiItem> = listOf(
				backButton,
				searchButton
			)

			val items: List<GuiItem> = getGuiItems(territoryId, remote, getCityItems(territoryId))

			val cityName = Territory.findPropById(territoryId, Territory::name)

			Tasks.sync {
				player.openPaginatedMenu("City: $cityName", items, titleButtons)
			}
		}
	}

	fun getGuiItems(territoryId: Oid<Territory>, remote: Boolean, bazaarItems: FindIterable<BazaarItem>): List<GuiItem> = getGuiItems(
		territoryId, remote, bazaarItems.descendingSort(BazaarItem::stock).toList()
	)

	fun getGuiItems(territoryId: Oid<Territory>, remote: Boolean, bazaarItems: List<BazaarItem>): List<GuiItem> =
		bazaarItems
			.map(BazaarItem::itemString)
			// only one per item string
			.distinct()
			// convert to GuiItem
			.map { itemString ->
				val item: ItemStack = fromItemString(itemString)
				return@map MenuHelper.guiButton(item) {
					openItemMenu(playerClicker, territoryId, itemString, SortingBy.STOCK, true, remote)
				}
			}

	private fun getCityItems(territoryId: Oid<Territory>): FindIterable<BazaarItem> = BazaarItem
		.find(and(BazaarItem::cityTerritory eq territoryId, BazaarItem::stock gt 0))

	enum class SortingBy(val property: KProperty<*>, val displayType: Material) {
		PRICE(BazaarItem::price, Material.GOLD_INGOT),
		STOCK(BazaarItem::stock, Material.NAME_TAG)
	}

	private fun openItemMenu(
        player: Player,
        terrId: Oid<Territory>,
        item: String,
        sort: SortingBy,
        descend: Boolean,
        remote: Boolean
	): Unit = Tasks.async {
		val city: TradeCityData = TradeCities.getIfCity(Regions[terrId])
			?: return@async player.serverError("Territory is no longer a city")
		MenuHelper.run {
			val lore = listOf("Left click to sort descending,", "right click to sort ascending.")
			val titleItems: List<GuiItem> = SortingBy.values().map { newSort ->
				guiButton(newSort.displayType) {
					playerClicker.closeInventory()
					openItemMenu(playerClicker, terrId, item, newSort, isLeftClick, remote)
				}.setName("Sort By $newSort").setLore(lore)
			} + guiButton(Material.IRON_DOOR) { openMainMenu(terrId, playerClicker, remote) }.setName("Go Back")

			val items: List<GuiItem> = BazaarItem
				.find(and(BazaarItem::cityTerritory eq terrId, BazaarItem::itemString eq item, BazaarItem::stock gt 0))
				.let { if (descend) it.descendingSort(sort.property) else it.ascendingSort(sort.property) }
				.map { bazaarItem ->
					val itemStack = fromItemString(bazaarItem.itemString)
					val sellerName = SLPlayer.getName(bazaarItem.seller)
						?: error("Failed to get name of ${bazaarItem.seller}")
					val priceString = bazaarItem.price.toCreditsString()
					val stock = bazaarItem.stock
					return@map guiButton(itemStack) {
						//openPurchaseMenu(playerClicker, bazaarItem, sellerName, 0, remote)
						BazaarPurchaseMenuGui(
							viewer = playerClicker,
							bazaarItem = bazaarItem,
							sellerName = sellerName,
							remote = remote,
							returnCallback = {
								playerClicker.closeInventory()
								openItemMenu(player, terrId, item, sort, descend, remote)
							},
							confirmCallback = ::tryBuy
						).openGui()
					}.setName(priceString).setLore(listOf("Seller: $sellerName", "Stock: $stock"))
				}
				.toList()

			val name = fromItemString(item).displayNameString

			Tasks.sync {
				player.openPaginatedMenu("$name @ ${city.displayName}", items, titleItems)
			}
		}
	}

	fun priceMult(remote: Boolean) = if (remote) 4 else 1

	private fun search(territoryId: Oid<Territory>, search: String): List<BazaarItem> =
		getCityItems(territoryId).filter { it.itemString.contains(search, true) }

	fun tryBuy(player: Player, item: BazaarItem, amount: Int, remote: Boolean, resultConsumer: Consumer<InputResult> = Consumer {}) {
		val price: Double = item.price
		val revenue: Double = amount * price
		val priceMult = priceMult(remote)
		val cost: Double = revenue * priceMult

		if (!VAULT_ECO.has(player, cost)) {
			player.userError(
				"You can't afford that! Cost for $amount: ${cost.toCreditsString()}" +
					if (priceMult > 1) " (Price multiplied x $priceMult due to browsing remotely)" else ""
			)

			resultConsumer.accept(InputResult.FailureReason(listOf(
				text("You can't afford that!"),
				text("Cost for $amount: ${cost.toCreditsString()} ${if (priceMult > 1) " (Price multiplied x $priceMult due to browsing remotely)" else ""}")
			)))

			return
		}

		val city: TradeCityData? = TradeCities.getIfCity(Regions[item.cityTerritory])

		if (city == null) {
			resultConsumer.accept(InputResult.FailureReason(listOf(
				text("${Regions.get<RegionTerritory>(item.cityTerritory).name} is no longer a trade city!", NamedTextColor.RED),
			)))

			return
		}

		Tasks.async {
			if (!BazaarItem.hasStock(item._id, amount)) {
				resultConsumer.accept(InputResult.FailureReason(listOf(
					text("Item no longer has $amount in stock", NamedTextColor.RED),
				)))

				return@async player.information("Item no longer has $amount in stock")
			}

			if (BazaarItem.matches(item._id, BazaarItem::price ne price)) {
				resultConsumer.accept(InputResult.FailureReason(listOf(
					text("Price has changed", NamedTextColor.RED),
				)))

				return@async player.userError("Price has changed")
			}

			val itemStack = fromItemString(item.itemString)

			BazaarItem.removeStock(item._id, amount)
			item.stock -= amount

			val tax = (city.tax * revenue).roundToInt()
			BazaarItem.depositMoney(item._id, revenue - tax)
			if (city.type == TradeCityType.SETTLEMENT) {
				Settlement.deposit(city.settlementId, tax)
			}

			Tasks.sync {
				VAULT_ECO.withdrawPlayer(player, cost)
				val (fullStacks, remainder) = giveOrDropItems(itemStack, amount, player)

				val quantityMessage = if (itemStack.maxStackSize == 1) "{0}" else "{0} stack${if (fullStacks == 1) "" else "s"} and {1} item${if (remainder == 1) "" else "s"}"

				// Don't include the price mult in the initial message so it can be done in a 2nd line for the input result
				val fullMessage = template(
					text("Bought $quantityMessage of {2} for {3}", GREEN),
					fullStacks,
					remainder,
					itemStack.displayNameComponent,
					cost.toCreditComponent(),
				)

				val priceMultiplicationMessage = ofChildren(text("(Price multiplied by ", YELLOW), text(priceMult, WHITE), text(" due to browsing remotely)", YELLOW))

				player.sendMessage(ofChildren(
					fullMessage,
					if (priceMult > 1) ofChildren(space(), priceMultiplicationMessage) else empty()
				))

				resultConsumer.accept(InputResult.SuccessReason(listOf(
					fullMessage,
					if (priceMult > 1) priceMultiplicationMessage
					else empty()
				)))
			}
		}
	}

	/**
	 * Gives the player items, or drops them at their location if their inventory is full.
	 *
	 * Returns a pair of full stacks of items to the remainder
	 **/
	fun giveOrDropItems(itemStack: ItemStack, amount: Int, sender: Player): Pair<Int, Int> {
		val maxStackSize = itemStack.maxStackSize
		val fullStacks = amount / maxStackSize

		fun add(amount: Int) {
			val stack = itemStack.clone().apply { this.amount = amount }
			val remainder: HashMap<Int, ItemStack> = sender.inventory.addItem(stack)

			// remainder is when the inventory didn't have space

			for (remainingItem in remainder.values) {
				sender.world.dropItem(sender.eyeLocation, remainingItem)
			}
		}

		repeat(fullStacks) { add(maxStackSize) }
		val remainder = amount % maxStackSize
		if (remainder > 0) {
			add(remainder)
		}
		return Pair(fullStacks, remainder)
	}

	fun cleanExpiredBazaarEntries() {

	}

	fun createListing(): InputResult {
		return InputResult.InputFailure
	}

	fun createOrder(player: Player, territory: RegionTerritory, itemString: String, orderQuantity: Int, individualPrice: Double): InputResult {
		val cityName = TradeCities.getIfCity(territory)?.displayName ?: return InputResult.FailureReason(listOf(text("${territory.name} is not a trade city!")))

		try {
			BazaarOrder.create(player.slPlayerId, territory.id, itemString, orderQuantity, individualPrice)
			player.information("Created a bazaar order for $orderQuantity of $itemString for $individualPrice per item at $cityName.")

			return InputResult.SuccessReason(listOf(
				text("Created an order for $itemString at $cityName.", GREEN)
			))
		} catch (e: Throwable) {
			return InputResult.FailureReason(listOf(
				text("There was an error adding your order. Please forward this to staff.", RED),
				formatException(e)
			))
		}
	}

	fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName ?: "<{Unknown}>"
	// this will be used if the city is disbanded but their items remain there
}
