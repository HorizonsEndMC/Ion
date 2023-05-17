package net.starlegacy.feature.economy.bazaar

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.customitems.CustomItems.customItem
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.SLComponent
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.BazaarItem
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.city.TradeCityType
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.displayNameString
import net.starlegacy.util.toCreditsString
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.ascendingSort
import org.litote.kmongo.descendingSort
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.ne
import kotlin.math.absoluteValue
import kotlin.math.roundToInt
import kotlin.reflect.KProperty

object Bazaars : SLComponent() {
	override fun supportsVanilla(): Boolean {
		return true
	}

	fun onClickBazaarNPC(player: Player, city: TradeCityData) {
		val territoryId: Oid<Territory> = city.territoryId

		openMainMenu(territoryId, player, false)
	}

	fun openMainMenu(territoryId: Oid<Territory>, player: Player, remote: Boolean) = Tasks.async {
		MenuHelper.run {
			val items: List<GuiItem> = BazaarItem
				.find(and(BazaarItem::cityTerritory eq territoryId, BazaarItem::stock gt 0))
				.descendingSort(BazaarItem::stock)
				.map(BazaarItem::itemString)
				// only one per item string
				.distinct()
				// convert to GuiItem
				.map { itemString ->
					val item: ItemStack = fromItemString(itemString)
					return@map guiButton(item) {
						openItemMenu(playerClicker, territoryId, itemString, SortingBy.STOCK, true, remote)
					}
				}

			Tasks.sync {
				player.openPaginatedMenu("Select An Item", items)
			}
		}
	}

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
						openPurchaseMenu(playerClicker, bazaarItem, sellerName, 0, remote)
					}.setName(priceString).setLore(listOf("Seller: $sellerName", "Stock: $stock"))
				}
				.toList()

			val name = fromItemString(item).displayNameString

			Tasks.sync {
				player.openPaginatedMenu("$name @ ${city.displayName}", items, titleItems)
			}
		}
	}

	private fun priceMult(remote: Boolean) = if (remote) 4 else 1

	private fun openPurchaseMenu(
		player: Player,
		item: BazaarItem,
		sellerName: String,
		currentAmount: Int,
		remote: Boolean
	) {
		MenuHelper.apply {
			val pane = outlinePane(0, 0, 9, 1)

			val priceMult = priceMult(remote)

			fun addButton(amount: Int) {
				val buttonType = if (amount < 0) Material.RED_STAINED_GLASS_PANE else Material.LIME_STAINED_GLASS_PANE
				val buttonItem = ItemStack(buttonType)
				buttonItem.amount = amount.absoluteValue
				pane.addItem(
					guiButton(buttonItem) {
						val newAmount = currentAmount + amount
						if (item.stock - newAmount >= 0 && newAmount >= 0) {
							val cost: Double = newAmount * item.price * priceMult

							if (!VAULT_ECO.has(playerClicker, cost)) {
								playerClicker.userError(
									"You don't have enough credits! Cost for $newAmount: ${cost.toCreditsString()}" +
										if (priceMult > 1) " (Price multiplied x $priceMult due to browsing remotely)" else ""
								)
							} else {
								openPurchaseMenu(playerClicker, item, sellerName, newAmount, remote)
							}
						}
					}.setName((if (amount < 0) "Subtract" else "Add") + " ${amount.absoluteValue}")
				)
			}

			addButton(-32)
			addButton(-8)
			addButton(-1)
			addButton(1)
			addButton(8)
			addButton(32)
			addButton(64)

			pane.addItem(
				guiButton(Material.IRON_DOOR) {
					openItemMenu(playerClicker, item.cityTerritory, item.itemString, SortingBy.STOCK, true, remote)
				}.setName("Go Back")
			)

			val name = fromItemString(item.itemString).displayNameString

			if (currentAmount == 0) {
				pane.addItem(guiButton(Material.BARRIER).setName("Buy at least one item"))
			} else {
				val lore = mutableListOf<String>()

				lore += "&fBuy $currentAmount of $name for ${item.price * currentAmount * priceMult}"

				if (priceMult > 1) {
					lore += "(Price multiplied x $priceMult due to browsing remotely)"
				}

				pane.addItem(
					guiButton(Material.HOPPER) {
						playerClicker.closeInventory()
						tryBuy(playerClicker, item, currentAmount, remote)
					}.setName(Component.text("Purchase").color(NamedTextColor.GREEN)).setLore(lore)
				)
			}

			gui(1, "$currentAmount/${item.stock} $sellerName's $name").withPane(pane).show(player)
		}
	}

	private fun tryBuy(player: Player, item: BazaarItem, amount: Int, remote: Boolean) {
		val price: Double = item.price
		val revenue: Double = amount * price
		val priceMult = priceMult(remote)
		val cost: Double = revenue * priceMult

		if (!VAULT_ECO.has(player, cost)) {
			return player.userError(
				"You can't afford that! Cost for $amount: ${cost.toCreditsString()}" +
					if (priceMult > 1) " (Price multiplied x $priceMult due to browsing remotely)" else ""
			)
		}

		val city: TradeCityData = TradeCities.getIfCity(Regions[item.cityTerritory]) ?: return

		Tasks.async {
			if (!BazaarItem.hasStock(item._id, amount)) {
				return@async player.information("Item no longer has $amount in stock")
			}

			if (BazaarItem.matches(item._id, BazaarItem::price ne price)) {
				return@async player.userError("Price has changed")
			}

			val itemStack = fromItemString(item.itemString)

			BazaarItem.removeStock(item._id, amount)

			val tax = (city.tax * revenue).roundToInt()
			BazaarItem.depositMoney(item._id, revenue - tax)
			if (city.type == TradeCityType.SETTLEMENT) {
				Settlement.deposit(city.settlementId, tax)
			}

			Tasks.sync {
				VAULT_ECO.withdrawPlayer(player, cost)
				val (fullStacks, remainder) = dropItems(itemStack, amount, player)

				val buyMessage = Component.text().color(NamedTextColor.GREEN)
					.append(Component.text("Bought "))
					.append(Component.text(fullStacks).color(NamedTextColor.WHITE))

				if (itemStack.maxStackSize == 1) {
					buyMessage
						.append(Component.text(" "))
						.append(
							itemStack.displayNameComponent.append(
								if (fullStacks == 1) Component.text("") else Component.text("s")
							)
						)
				} else {
					buyMessage
						.append(if (fullStacks == 1) Component.text(" stack and ") else Component.text(" stacks and "))
						.append(Component.text(remainder).color(NamedTextColor.WHITE))
						.append(if (remainder == 1) Component.text(" item") else Component.text(" items"))
						.append(Component.text(" of "))
						.append(itemStack.displayNameComponent)
				}

				buyMessage
					.append(Component.text(" for "))
					.append(Component.text(cost.toCreditsString()).color(NamedTextColor.GOLD))

				if (priceMult > 1) {
					buyMessage
						.append(Component.text(" (Price multiplied by ").color(NamedTextColor.YELLOW))
						.append(Component.text(priceMult).color(NamedTextColor.WHITE))
						.append(Component.text(" due to browsing remotely)").color(NamedTextColor.YELLOW))
				}

				player.sendMessage(
					buyMessage
				)
			}
		}
	}

	fun toItemString(item: ItemStack): String {
		return item.customItem?.identifier ?: CustomItems[item]?.id ?: item.type.toString()
	}

	fun fromItemString(string: String): ItemStack {
		// if a custom item is found, use that

		net.horizonsend.ion.server.features.customitems.CustomItems.getByIdentifier(string)?.let { return it.constructItemStack() }
		CustomItems[string]?.let { return it.itemStack(1) }
		val material: Material = Material.valueOf(string)
		check(material.isItem) { "$material is not an item" }
		return ItemStack(material, 1)
	}

	fun dropItems(itemStack: ItemStack, amount: Int, sender: Player): Pair<Int, Int> {
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
}
