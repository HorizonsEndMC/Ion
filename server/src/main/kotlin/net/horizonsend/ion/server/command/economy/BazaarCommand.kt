package net.horizonsend.ion.server.command.economy

import co.aikar.commands.InvalidCommandArgument
import co.aikar.commands.PaperCommandManager
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatLink
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.GlobalCompletions.toItemString
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.economy.city.CityNPCs.BAZAAR_CITY_TERRITORIES
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.manage.ListListingManagementMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.ItemMenu
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.updateLore
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import okhttp3.FormBody
import okhttp3.OkHttpClient
import okhttp3.Request
import org.bukkit.DyeColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import java.time.Duration
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.UUID
import kotlin.math.ceil

@CommandAlias("bazaar|ah|auctionhouse|shop|buy")
object BazaarCommand : SLCommand() {
	private val exportCooldown: MutableMap<UUID, Long> = mutableMapOf()
	private const val TIME_BETWEEN_EXPORTS_MIN = 15L

	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "bazaarItemStrings") { context ->
			val player = context.player ?: throw InvalidCommandArgument("Players only")
			val territory = Regions.findFirstOf<RegionTerritory>(player.location) ?: throw InvalidCommandArgument("You're not in a territory!")

			BazaarItem.findProp(
				and(BazaarItem::seller eq player.slPlayerId, BazaarItem::cityTerritory eq territory.id),
				BazaarItem::itemString
			).toList()
		}

		registerAsyncCompletion(manager, "bazaarCities") { _ ->
			TradeCities.getAll().filter { BAZAAR_CITY_TERRITORIES.contains(it.territoryId) }.map { it.displayName }
		}

		manager.commandContexts.registerContext(TradeCityData::class.java) { c ->
			val name = c.popFirstArg()
			TradeCities.getAll().firstOrNull { it.displayName == name }  ?: throw InvalidCommandArgument("Trade city $name not found!")
		}

		registerAsyncCompletion(manager, "playerOrders") { c ->
			val city = c.getContextValue(TradeCityData::class.java) ?: throw InvalidCommandArgument("No trade city specified!")
			BazaarOrder.find(and(BazaarOrder::player eq c.player.slPlayerId, BazaarOrder::cityTerritory eq city.territoryId))
				.toList()
				.map { it.itemString }
		}

		registerAsyncCompletion(manager, "cityOrderers") { c ->
			val city = c.getContextValue(TradeCityData::class.java) ?: throw InvalidCommandArgument("No trade city specified!")
			BazaarOrder.findProp(BazaarOrder::cityTerritory eq city.territoryId, BazaarOrder::player).mapNotNull(SLPlayer::getName)
		}

		registerAsyncCompletion(manager, "cityOrders") { c ->
			val city = c.getContextValue(TradeCityData::class.java) ?: throw InvalidCommandArgument("No trade city specified!")
			val owner = c.getContextValue(SLPlayer::class.java) ?: throw InvalidCommandArgument("No trade player specified!")

			BazaarOrder.find(and(BazaarOrder::player eq owner._id, BazaarOrder::cityTerritory eq city.territoryId))
				.toList()
				.map { it.itemString }
		}
	}

	private fun requireItemInHand(sender: Player): ItemStack {
		val item = sender.inventory.itemInMainHand
		failIf(item.isEmpty) { "You aren't holding an item!" }

		return item
	}

	@Suppress("Unused")
	@Subcommand("browse")
	@Default
	@Description("Remotely browse city bazaar markets")
	fun onBrowse(sender: Player) {
		BazaarGUIs.openCitySelection(sender, null)
	}

	@Suppress("Unused")
	@Subcommand("sell")
	@Description("Remotely browse city bazaar markets")
	fun onBrowseOrders(sender: Player) {
		BazaarGUIs.openBuyOrderMainMenu(sender, null)
	}

	@Suppress("Unused")
	@Subcommand("string")
	fun onString(sender: Player) {
		val item = requireItemInHand(sender)
		sender.information("Item string of ${item.displayNameString}: ${toItemString(item)}")
	}

	@Subcommand("create")
	@Description("Create a new listing at this city")
	@CommandCompletion("@anyItem")
	fun onListingCreate(sender: Player, itemString: String, pricePerItem: Double) = asyncCommand(sender) {
		val territory: RegionTerritory = requireTerritoryIn(sender)
		Bazaars.createListing(sender, territory, itemString, pricePerItem).sendReason(sender)
	}

	@Subcommand("create")
	@Description("Create a new listing at this city")
	fun onListingCreate(sender: Player, pricePerItem: Double) = asyncCommand(sender) {
		val item = requireItemInHand(sender)
		val itemString = toItemString(item)
		onListingCreate(sender, itemString, pricePerItem)
	}

	@Suppress("Unused")
	@Subcommand("deposit")
	@Description("Deposit all matching items in your inventory")
	fun onDeposit(sender: Player, @Optional limit: Int?) = asyncCommand(sender) {
		val item = requireItemInHand(sender)
		val itemString = toItemString(item)
		onDeposit(sender, itemString, limit)
	}

	@Suppress("Unused")
	@Subcommand("deposit")
	@Description("Deposit all matching items in your inventory")
	@CommandCompletion("@bazaarItemStrings")
	fun onDeposit(sender: Player, itemString: String, @Optional limit: Int?) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		Bazaars.depositListingStock(sender, sender.inventory, territory, itemString, limit ?: Int.MAX_VALUE).sendReason(sender)
	}

	@Suppress("Unused")
	@Subcommand("withdraw")
	@Description("Withdraw the specified amount of the item")
	@CommandCompletion("@bazaarItemStrings 1|64")
	fun onWithdraw(sender: Player, itemString: String, amount: Int) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		Bazaars.withdrawListingBalance(sender, territory, itemString, amount).sendReason(sender)
	}

	@Suppress("Unused")
	@Subcommand("remove")
	@Description("Remove a listing from the bazaar at this city")
	@CommandCompletion("@bazaarItemStrings")
	fun onRemove(sender: Player, itemString: String) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		Bazaars.removeListing(sender, territory, itemString).sendReason(sender)
	}

	@Suppress("Unused")
	@Subcommand("setprice")
	@Description("Update the price of the specific item")
	@CommandCompletion("@bazaarItemStrings @nothing")
	fun onSetPrice(sender: Player, itemString: String, newPrice: Double) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)

		Bazaars.setListingPrice(sender, territory, itemString, newPrice).sendReason(sender)
	}

	@Suppress("Unused")
	@Subcommand("list")
	@Description("List all of the items you're selling")
	fun onList(sender: Player, @Optional page: Int?) = asyncCommand(sender) {
		val items = BazaarItem.find(BazaarItem::seller eq sender.slPlayerId).toList()

		if (items.isEmpty()) return@asyncCommand sender.userError("You do not have any items listed on the bazaar.")

		val builder = text()

		builder.append(text("Your Items (${items.size})"), newline())
		builder.append(lineBreak(45), newline())

		var totalBalance = 0.0

		val body = formatPaginatedMenu(
			items.size,
			"/bazaar list",
			page ?: 1
		) { index ->
			val item = items[index]
			val itemDisplayName = fromItemString(item.itemString).displayNameComponent
			val city = cityName(Regions[item.cityTerritory])
			val stock = item.stock
			val uncollected = item.balance.toCreditComponent()
			val price = item.price.toCreditComponent()

			totalBalance += item.balance

			ofChildren(
				itemDisplayName,
				text(" @ ", DARK_PURPLE),
				text(city, LIGHT_PURPLE),
				bracketed(template(text("stock: {0}, balance: {1}, price: {2}", GRAY), stock, uncollected, price))
			)
		}

		builder.append(body, newline())
		builder.append(lineBreak(45), newline())
		builder.append(template(text("Total Uncollected Credits: {0}", HE_MEDIUM_GRAY), totalBalance.toCreditComponent()))

		sender.sendMessage(builder.build())
	}

	@Subcommand("export")
	@Description("Export sell orders in CSV format (copies to clipboard)")
	@CommandCompletion("@bazaarCities")
	fun onExport(sender: Player, city: TradeCityData) = asyncCommand(sender) {
		// Prevent users from spamming API requests
		val cooldownMillis = exportCooldown[sender.uniqueId] ?: 0
		failIf(exportOnCooldown(sender)) {
			"You must wait $TIME_BETWEEN_EXPORTS_MIN minutes before requesting another export " +
					"(current time left: ${TIME_BETWEEN_EXPORTS_MIN - (Duration.ofMillis(System.currentTimeMillis() - cooldownMillis).toMinutes())})"
		}

		val items = BazaarItem.find(BazaarItem::cityTerritory eq city.territoryId).toList()

		if (items.isEmpty()) return@asyncCommand sender.userError("This city does not have any items listed on the bazaar.")

		// Construct CSV string
		val stringBuilder: StringBuilder = StringBuilder("Seller,Trade City,Item,Price,Stock,Balance").appendLine()

		for (item in items) {
			stringBuilder.appendLine(sender.name + ',' +
					cityName(Regions[item.cityTerritory]) + ',' +
					item.itemString + ',' +
					item.price.roundToHundredth() + ',' +
					item.stock + ',' +
                    item.balance.roundToHundredth()
			)
		}

		// Construct HTTP request
		val httpClient = OkHttpClient()
		val request = createPastebinHttpRequest(stringBuilder.toString(), sender.name + "_Bazaar_Export_" +
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".csv")
		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				sender.userError("Failed to export bazaar sell order data (${response.code}, ${response.body?.string()})")
				return@asyncCommand
			}

			// Set new export cooldown
			exportCooldown[sender.uniqueId] = System.currentTimeMillis()
			sender.success("Exported bazaar sell order data as CSV (expires in 10 minutes): ")
			val responseBody = response.body?.string() ?: "null"
			sender.sendMessage(bracketed(formatLink(responseBody, responseBody)))
		}
	}

	@Suppress("Unused")
	@Subcommand("list menu")
	@Description("List the items you're selling at this city")
	fun onListMenu(sender: Player) = asyncCommand(sender) {
		ListListingManagementMenu(sender).openGui()
	}

	@Suppress("Unused")
	@Subcommand("list gui")
	@Description("List the items you're selling at this city")
	fun onListGui(sender: Player) = asyncCommand(sender) {
		ListListingManagementMenu(sender).openGui()
	}

	@Suppress("Unused")
	@Subcommand("collect")
	@Description("Collect the money from all of your items")
	fun onCollect(sender: Player) = asyncCommand(sender) {
		Bazaars.collectListingProfit(sender).sendReason(sender)
	}

	@Suppress("Unused")
	@Subcommand("tax")
	@Description("View the tax of the city you're in")
	fun onTax(sender: Player) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val city = TradeCities.getIfCity(territory) ?: fail { "You're not in a trade city" }
		sender.information("Tax of ${city.displayName}: ${(city.tax * 100).toInt()}%")
	}

	@Suppress("Unused")
	@Subcommand("merchant buy")
	fun onMerchantBuy(sender: Player, itemString: String, amount: Int) {
		requireEconomyEnabled()

		val itemValidationResult = Bazaars.checkValidString(itemString)
		val item: ItemStack? = itemValidationResult.result
		if (item == null) {
			itemValidationResult.sendReason(sender)
			return
		}

		val npc = CityNpcCommand.requireNearbyNPC(sender, false)
		failIf(npc.type != CityNPC.Type.MERCHANT) { "Nearest NPC is not a merchant" }
		val price = (Merchants.getPrice(itemString) ?: fail { "Item not for sale!" }) * amount
		val city = TradeCities.getIfCity(Regions[npc.territory]) ?: return
		val tax = ceil(city.tax * price).toInt()
		requireMoney(sender, price + tax)
		VAULT_ECO.withdrawPlayer(sender, price + tax)
		Bazaars.giveOrDropItems(item, amount, sender)

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
		val itemResult = Bazaars.checkValidString(itemString)
		if (!itemResult.isSuccess()) return itemResult.sendReason(sender)
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
		ItemMenu(
			title = text("Merchant Prices"),
			viewer = sender,
			guiItems = Merchants.getPriceMap().entries
				.toList()
				.sortedBy {
					val key = it.key

					val colorPattern = """(${DyeColor.entries.joinToString("|")})_""".toRegex(RegexOption.DOT_MATCHES_ALL)
					val colorMatch = colorPattern.find(key)
					if (colorMatch != null) {
						return@sortedBy key.removePrefix(colorMatch.value) + colorMatch.value
					}

					return@sortedBy key
				}
				.map { (itemString, price) ->
					fromItemString(itemString)
						.updateLore(listOf(template(text("Price: {0}", HE_MEDIUM_GRAY), price.toCreditComponent())))
						.makeGuiButton { _, _ -> sender.closeInventory() }
				},
			backButtonHandler = { sender.closeInventory() }
		).openGui()
	}

	@Subcommand("order create")
	@Description("Create a new buy order at this city")
	@CommandCompletion("@anyItem 1|10|100 1.0|10.0|100.0 @nothing")
	fun onOrderCreate(sender: Player, itemString: String, quantity: Int, pricePerItem: Double, @Optional priceConfirmation: Double?) = asyncCommand(sender) {
		val territory: RegionTerritory = requireTerritoryIn(sender)
		val realCost = quantity * pricePerItem

		failIf(priceConfirmation != realCost) {
			"You must acknowledge the cost of the listing to create it. The cost is ${realCost.toCreditsString()}. Run the command: /bazaar order create $itemString $quantity $pricePerItem $realCost"
		}

		Bazaars.createOrder(sender, territory, itemString, quantity, pricePerItem).sendReason(sender)
	}

	@Subcommand("order create")
	@Description("Create a new buy order at this city")
	@CommandCompletion("@anyItem @bazaarCities 1|10|100 1.0|10.0|100.0 @nothing")
	fun onOrderCreate(sender: Player, itemString: String, city: TradeCityData, quantity: Int, pricePerItem: Double, @Optional priceConfirmation: Double?) = asyncCommand(sender) {
		val territory: RegionTerritory = Regions[city.territoryId]
		val realCost = quantity * pricePerItem

		failIf(priceConfirmation != realCost) {
			"You must acknowledge the cost of the listing to create it. The cost is ${realCost.toCreditsString()}. Run the command: /bazaar order create $itemString $quantity $pricePerItem $realCost"
		}

		Bazaars.createOrder(sender, territory, itemString, quantity, pricePerItem).sendReason(sender)
	}

	@Subcommand("order remove")
	@Description("Create a new buy order at the provided city")
	@CommandCompletion("@bazaarCities @playerOrders")
	fun onOrderDelete(sender: Player, city: TradeCityData, orderString: String) = asyncCommand(sender) {
		val orderCheck = Bazaars.checkHasOrder(sender.slPlayerId, Regions[city.territoryId], orderString)
		val order = orderCheck.result ?: return@asyncCommand orderCheck.sendReason(sender)

		Bazaars.deleteOrder(sender, order._id).sendReason(sender)
	}

	@Subcommand("order withdraw")
	@Description("Withdraws fulfilled items from this order. You may optionally specify a limit.")
	@CommandCompletion("@bazaarCities @playerOrders")
	fun onOrderWithdraw(sender: Player, city: TradeCityData, orderString: String, @Optional limit: Int?) = asyncCommand(sender) {
		val orderCheck = Bazaars.checkHasOrder(sender.slPlayerId, Regions[city.territoryId], orderString)
		val order = orderCheck.result ?: return@asyncCommand orderCheck.sendReason(sender)

		Bazaars.withdrawOrderStock(sender, order._id, limit ?: Int.MAX_VALUE).sendReason(sender)
	}

	@Subcommand("order fulfill")
	@Description("Fulfills the order at the provided city.")
	@CommandCompletion("@bazaarCities @cityOrderers @cityOrders")
	fun onOrderFulfill(sender: Player, city: TradeCityData, owner: SLPlayer, orderString: String, @Optional limit: Int?) = asyncCommand(sender) {
		val orderCheck = Bazaars.checkHasOrder(owner._id, Regions[city.territoryId], orderString)
		val order = orderCheck.result ?: return@asyncCommand orderCheck.sendReason(sender)

		Bazaars.fulfillOrder(sender, sender.inventory, order._id, limit ?: Int.MAX_VALUE).sendReason(sender)
	}

	@Subcommand("order export")
	@Description("Export buy orders in CSV format (copies to clipboard)")
	@CommandCompletion("@bazaarCities")
	fun onOrderExport(sender: Player, city: TradeCityData) = asyncCommand(sender) {
		// Prevent users from spamming API requests
		val cooldownMillis = exportCooldown[sender.uniqueId] ?: 0
		failIf(exportOnCooldown(sender)) {
			"You must wait $TIME_BETWEEN_EXPORTS_MIN minutes before requesting another export " +
					"(current time left: ${TIME_BETWEEN_EXPORTS_MIN - (Duration.ofMillis(System.currentTimeMillis() - cooldownMillis).toMinutes())})"
		}

		val items = BazaarOrder.find(BazaarOrder::cityTerritory eq city.territoryId).toList()

		if (items.isEmpty()) return@asyncCommand sender.userError("This city does not have any orders on the bazaar.")

		// Construct CSV string
		val stringBuilder: StringBuilder = StringBuilder("Buyer,Trade City,Item,Price,Balance,Requested Quantity,Fulfilled Quantity,Stock").appendLine()

		for (item in items) {
			stringBuilder.appendLine(sender.name + ',' +
					cityName(Regions[item.cityTerritory]) + ',' +
					item.itemString + ',' +
					item.pricePerItem.roundToHundredth() + ',' +
					item.balance.roundToHundredth() + ',' +
					item.requestedQuantity + ',' +
					item.fulfilledQuantity + ',' +
					item.stock
			)
		}

		// Construct HTTP request
		val httpClient = OkHttpClient()
		val request = createPastebinHttpRequest(stringBuilder.toString(), sender.name + "_Bazaar_Order_Export_" +
				LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd-HH-mm")) + ".csv")
		httpClient.newCall(request).execute().use { response ->
			if (!response.isSuccessful) {
				sender.userError("Failed to export bazaar sell order data (${response.code}, ${response.body?.string()})")
				return@asyncCommand
			}

			// Set new export cooldown
			exportCooldown[sender.uniqueId] = System.currentTimeMillis()
			sender.success("Exported bazaar buy order data as CSV (expires in 10 minutes): ")
			val responseBody = response.body?.string() ?: "null"
			sender.sendMessage(bracketed(formatLink(responseBody, responseBody)))
		}
	}

	private fun exportOnCooldown(sender: Player): Boolean {
		val cooldownMillis = exportCooldown[sender.uniqueId]
		return cooldownMillis != null && Duration.ofMillis(System.currentTimeMillis() - cooldownMillis).toMinutes() < TIME_BETWEEN_EXPORTS_MIN
	}

	private fun createPastebinHttpRequest(body: String, name: String): Request {
		val body = FormBody.Builder()
			.add("api_dev_key", ConfigurationFiles.serverConfiguration().pastebinApiDevKey ?: "")
			.add("api_option", "paste")
			.add("api_paste_code", body)
			.add("api_paste_private", "1")
			.add("api_paste_expire_date", "10M")
			.add("api_paste_name", name)
			.build()
		return Request.Builder()
			.url("https://pastebin.com/api/api_post.php")
			.post(body)
			.build()
	}
}
