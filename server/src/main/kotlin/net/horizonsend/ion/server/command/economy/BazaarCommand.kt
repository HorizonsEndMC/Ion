package net.horizonsend.ion.server.command.economy

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItem
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.DyeColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import kotlin.math.ceil

@CommandAlias("bazaar")
object BazaarCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "bazaarItemStrings") { c ->
			val player = c.player ?: throw InvalidCommandArgument("Players only")
			val slPlayerId = player.slPlayerId
			val territory = Regions.findFirstOf<RegionTerritory>(player.location)
				?: throw InvalidCommandArgument("You're not in a territory!")
			BazaarItem.findProp(
				and(BazaarItem::seller eq slPlayerId, BazaarItem::cityTerritory eq territory.id),
				BazaarItem::itemString
			).toList()
		}

		registerAsyncCompletion(manager, "possibleBazaarItemStrings") { Bazaars.strings }
	}

	private fun validateItemString(itemString: String): ItemStack {
		try {
			val itemStack = Bazaars.fromItemString(itemString)
			failIf(!itemStack.type.isItem) { "$itemString is not an inventory item!" }
			return itemStack
		} catch (e: Exception) {
			fail { "Invalid item string $itemString! To see an item's string, use /bazaar string" }
		}
	}

	private fun validatePrice(price: Double) {
		failIf(price <= 0) { "Price must be above 0" }
		failIf(price != price.roundToHundredth()) { "Price cannot go further than 2 decimal places" }
	}

	private fun requireItemInHand(sender: Player) = sender.inventory.itemInMainHand

	@Suppress("Unused")
	@Subcommand("string")
	fun onString(sender: Player) {
		val item = requireItemInHand(sender)
		sender.information(
			"Item string of ${item.displayNameString}: ${Bazaars.toItemString(item)}"
		)
	}

	private fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName
		?: "<{Unknown}>" // this will be used if the city is disbanded but their items remain there

	@Subcommand("create")
	@Description("Create a new listing at this city")
	@CommandCompletion("@possibleBazaarItemStrings")
	fun onCreate(sender: Player, itemString: String, pricePerItem: Double) = asyncCommand(sender) {
		val territory: RegionTerritory = requireTerritoryIn(sender)
		failIf(!TradeCities.isCity(territory)) { "Territory is not a trade city" }
		failIf(!CityNPCs.BAZAAR_CITY_TERRITORIES.contains(territory.id)) { "City doesn't have a registered bazaar" }

		val cityName = cityName(territory)
		validatePrice(pricePerItem)
		validateItemString(itemString)

		failIf(!BazaarItem.none(BazaarItem.matchQuery(territory.id, sender.slPlayerId, itemString))) {
			"You're already selling $itemString at $cityName!"
		}

		BazaarItem.create(territory.id, sender.slPlayerId, itemString, pricePerItem)

		sender.information(
			"Created listing for $itemString at $cityName. " +
				"It will not show in the listing until it has some stock. " +
				"To add stock, use /bazaar deposit."
		)
	}

	@Subcommand("create")
	@Description("Create a new listing at this city")
	fun onCreate(sender: Player, pricePerItem: Double) = asyncCommand(sender) {
		val item = requireItemInHand(sender)
		val itemString = Bazaars.toItemString(item)

		onCreate(sender, itemString, pricePerItem)
	}

	private fun requireSelling(territory: RegionTerritory, sender: Player, itemString: String) =
		BazaarItem.findOne(BazaarItem.matchQuery(territory.id, sender.slPlayerId, itemString))
			?: fail { "You're not selling $itemString at ${cityName(territory)}" }

	@Suppress("Unused")
	@Subcommand("deposit")
	@Description("Deposit all matching items in your inventory")
	@CommandCompletion("@bazaarItemStrings")
	fun onDeposit(sender: Player) = asyncCommand(sender) {
		val item = requireItemInHand(sender)
		val itemString = Bazaars.toItemString(item)

		onDeposit(sender, itemString)
	}

	@Suppress("Unused")
	@Subcommand("deposit")
	@Description("Deposit all matching items in your inventory")
	@CommandCompletion("@bazaarItemStrings")
	fun onDeposit(sender: Player, itemString: String) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val cityName = cityName(territory)
		val itemReference: ItemStack = validateItemString(itemString)

		val item: BazaarItem = requireSelling(territory, sender, itemString)

		Tasks.sync {
			val inventory = sender.inventory

			var count = 0

			for ((index, itemStack) in inventory.withIndex()) {
				if (itemStack?.isSimilar(itemReference) == true) {
					count += itemStack.amount
					inventory.setItem(index, null)
				}
			}

			Tasks.async {
				BazaarItem.addStock(item._id, count)
				sender.information(
					"Added $count of $itemString to listing in $cityName"
				)
			}
		}
	}

	@Suppress("Unused")
	@Subcommand("withdraw")
	@Description("Withdraw the specified amount of the item")
	@CommandCompletion("@bazaarItemStrings 1|64")
	fun onWithdraw(sender: Player, itemString: String, amount: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val cityName = cityName(territory)
		val itemStack: ItemStack = validateItemString(itemString)
		failIf(amount < 1) { "Amount must be at least 1" }

		val item: BazaarItem = requireSelling(territory, sender, itemString)

		failIf(item.stock < amount) {
			"Your listing of $itemString at $cityName only has ${item.stock} item(s) in stock"
		}

		BazaarItem.removeStock(item._id, amount)

		Tasks.sync {
			val (fullStacks, remainder) = Bazaars.dropItems(itemStack, amount, sender)

			sender.success(
				"Withdraw $amount of $itemString at $cityName" +
					"($fullStacks stack(s) and $remainder item(s))"
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("remove")
	@Description("Remove a listing from the bazaar at this city")
	@CommandCompletion("@bazaarItemStrings")
	fun onRemove(sender: Player, itemString: String) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val cityName = cityName(territory)
		validateItemString(itemString)

		val item: BazaarItem = requireSelling(territory, sender, itemString)

		failIf(item.stock > 0) {
			"Withdraw all items before removing! (/bazaar withdraw $itemString ${item.stock})"
		}

		BazaarItem.delete(item._id)
		sender.success(
			"Removed listing for $itemString at $cityName"
		)
	}

	@Suppress("Unused")
	@Subcommand("setprice")
	@Description("Update the price of the specific item")
	@CommandCompletion("@bazaarItemStrings @nothing")
	fun onSetPrice(sender: Player, itemString: String, newPrice: Double) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val cityName = cityName(territory)
		validateItemString(itemString)

		val item: BazaarItem = requireSelling(territory, sender, itemString)

		validatePrice(newPrice)

		BazaarItem.setPrice(item._id, newPrice)
		sender.success(
			"Updated price of $itemString at $cityName to ${newPrice.toCreditsString()}"
		)
	}

	@Suppress("Unused")
	@Subcommand("list")
	@Description("List the items you're selling at this city")
	fun onList(sender: Player) = asyncCommand(sender) {
		val items = BazaarItem.find(BazaarItem::seller eq sender.slPlayerId).toList()
		sender.information("Your Items (${items.size})")
		for (item in items) {
			val itemDisplayName = Bazaars.fromItemString(item.itemString).displayNameComponent
			val city = cityName(Regions[item.cityTerritory])
			val stock = item.stock
			val uncollected = item.balance.toCreditsString()
			val price = item.price.toCreditsString()

			sender.sendMessage(
				text()
					.append(itemDisplayName)
					.append(text(" @ ").color(NamedTextColor.DARK_PURPLE))
					.append(text(city).color(NamedTextColor.LIGHT_PURPLE))
					.append(text(" [").color(NamedTextColor.DARK_GRAY))
					.append(text("stock: ").color(NamedTextColor.GRAY))
					.append(text(stock).color(NamedTextColor.GRAY))
					.append(text(", balance: ").color(NamedTextColor.GRAY))
					.append(text(uncollected).color(NamedTextColor.GOLD))
					.append(text(", price: ").color(NamedTextColor.GRAY))
					.append(text(price).color(NamedTextColor.YELLOW))
					.append(text("]").color(NamedTextColor.DARK_GRAY))
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("list menu")
	@Description("List the items you're selling at this city")
	fun onListMenu(sender: Player) = asyncCommand(sender) {
		val items = BazaarItem.find(BazaarItem::seller eq sender.slPlayerId).toList()

		MenuHelper.apply {
			val guiItems = items.map { item ->
				val city = cityName(Regions[item.cityTerritory])
				val stock = item.stock
				val uncollected = item.balance.toCreditsString()
				val price = item.price.toCreditsString()

				guiButton(Bazaars.fromItemString(item.itemString)).apply {
					setLoreComponent(listOf(
						text().append(text("City: ", NamedTextColor.DARK_PURPLE), text(city, NamedTextColor.LIGHT_PURPLE)).decoration(TextDecoration.ITALIC, false).build(),
						text().append(text("Stock: ", NamedTextColor.GRAY), text(stock, NamedTextColor.GRAY)).decoration(TextDecoration.ITALIC, false).build(),
						text().append(text("Balance: ", NamedTextColor.GRAY), text(uncollected, NamedTextColor.GOLD)).decoration(TextDecoration.ITALIC, false).build(),
						text().append(text("Price: ", NamedTextColor.GRAY), text(price, NamedTextColor.YELLOW)).decoration(TextDecoration.ITALIC, false).build(),
					))
				}
			}

			Tasks.sync { sender.openPaginatedMenu("Your Items (${items.size})", guiItems) }
		}
	}

	@Suppress("Unused")
	@Subcommand("collect")
	@Description("Collect the money from all of your items")
	fun onCollect(sender: Player) = asyncCommand(sender) {
		val senderId = sender.slPlayerId
		val total = BazaarItem.collectMoney(senderId)
		val count = BazaarItem.count(BazaarItem::seller eq senderId)
		Tasks.sync {
			VAULT_ECO.depositPlayer(sender, total)
			sender.success(
				"Collected ${total.toCreditsString()} from $count listings"
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("tax")
	@Description("View the tax of the city you're in")
	fun onTax(sender: Player) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val city = TradeCities.getIfCity(territory) ?: fail { "You're not in a trade city" }
		sender.information(
			"Tax of ${city.displayName}: ${(city.tax * 100).toInt()}%"
		)
	}

	@Suppress("Unused")
	@Subcommand("browse")
	@Default
	@Description("Remotely browse city bazaar markets")
	fun onBrowse(sender: Player) {
//		val sector = Sector.getSector(sender.world)

		val cities: List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
			.map { Regions.get<RegionTerritory>(it) }
//			.filter { Sector.getSector(it.world) == sector }
			.mapNotNull(TradeCities::getIfCity)

		MenuHelper.apply {
			val cityItems = cities.map { city ->
				val territoryId = city.territoryId
				val territory: RegionTerritory = Regions[territoryId]

				// attempt to get the planet icon, just use a detonator if unavailable
				val item: CustomItem = Space.getPlanet(territory.world)?.planetIcon ?: CustomItems.MINERAL_URANIUM

				return@map guiButton(item.itemStack(1)) {
					val clicker: Player = playerClicker
					val remote: Boolean = Regions.findFirstOf<RegionTerritory>(clicker.location)?.id != territoryId
					Bazaars.openMainMenu(territoryId, clicker, remote)
				}.setName("${city.displayName} on ${territory.world}")
			}

			sender.openPaginatedMenu("Remote Bazaar", cityItems)
		}
	}

	@Suppress("Unused")
	@Subcommand("merchant buy")
	fun onMerchantBuy(sender: Player, itemString: String, amount: Int) {
		val item = validateItemString(itemString)
		val npc = CityNpcCommand.requireNearbyNPC(sender, false)
		failIf(npc.type != CityNPC.Type.MERCHANT) { "Nearest NPC is not a merchant" }
		val price = (Merchants.getPrice(itemString) ?: fail { "Item not for sale!" }) * amount
		val city = TradeCities.getIfCity(Regions[npc.territory]) ?: return
		val tax = ceil(city.tax * price).toInt()
		requireMoney(sender, price + tax)
		VAULT_ECO.withdrawPlayer(sender, price + tax)
		Bazaars.dropItems(item, amount, sender)

		sender.sendMessage(
			text("Bought ").color(NamedTextColor.GREEN)
				.append(text(amount).color(NamedTextColor.WHITE))
				.append(text(" of "))
				.append(item.displayNameComponent)
				.append(text(" for "))
				.append(price.toCreditComponent())
				.append(text(" (+ "))
				.append(price.toCreditComponent())
				.append(text(" tax"))
		)

		if (city.type == TradeCityType.SETTLEMENT) {
			Settlement.deposit(city.settlementId, tax)
		}
	}

	@Suppress("Unused")
	@Subcommand("merchant setprice")
	@CommandPermission("trade.merchantadmin")
	fun onMerchantSetPrice(sender: CommandSender, itemString: String, price: Double) {
		validateItemString(itemString)
		Merchants.setMerchantDefaultPrice(itemString, price)
	}

	@Suppress("Unused")
	@Subcommand("merchant unsetprice")
	@CommandPermission("trade.merchantadmin")
	fun onMerchantUnsetPrice(sender: Player, itemString: String) {
		Merchants.removeMerchantItem(itemString)
	}

	@Suppress("Unused")
	@Subcommand("merchant prices")
	@Description("View merchant prices")
	fun onMerchantPrices(sender: Player) {
		MenuHelper.apply {
			val items = Merchants.getPriceMap().entries
				.asSequence()
				.sortedBy {
					val key = it.key

					val colorPattern = """(${DyeColor.values().joinToString("|")})_"""
						.toRegex(RegexOption.DOT_MATCHES_ALL)
					val colorMatch = colorPattern.find(key)
					if (colorMatch != null) {
						return@sortedBy key.removePrefix(colorMatch.value) + colorMatch.value
					}

					return@sortedBy key
				}
				.map { (itemString, price) ->
					val item = Bazaars.fromItemString(itemString)
					return@map guiButton(item) { playerClicker.closeInventory() }
						.setName(item.displayNameComponent)
						.setLore("Price: ${price.toCreditsString()}")
				}.toList()

			sender.openPaginatedMenu("Merchant Prices", items)
		}
	}
}
