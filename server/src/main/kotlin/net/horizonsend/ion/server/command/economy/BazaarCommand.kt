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
import net.horizonsend.ion.common.database.schema.economy.CityNPC
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.bracketed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatPaginatedMenu
import net.horizonsend.ion.common.utils.text.lineBreak
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.command.GlobalCompletions.toItemString
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars
import net.horizonsend.ion.server.features.economy.bazaar.Bazaars.cityName
import net.horizonsend.ion.server.features.economy.bazaar.Merchants
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.nations.gui.playerClicker
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.bazaar.purchase.window.manage.ListListingManagementMenu
import net.horizonsend.ion.server.miscellaneous.utils.MenuHelper
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.displayNameString
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.newline
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.DARK_PURPLE
import net.kyori.adventure.text.format.NamedTextColor.GRAY
import net.kyori.adventure.text.format.NamedTextColor.LIGHT_PURPLE
import org.bukkit.DyeColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import kotlin.math.ceil

@CommandAlias("bazaar|ah|auctionhouse|shop|buy")
object BazaarCommand : SLCommand() {
	override fun onEnable(manager: PaperCommandManager) {
		registerAsyncCompletion(manager, "bazaarItemStrings") { context ->
			val player = context.player ?: throw InvalidCommandArgument("Players only")
			val territory = Regions.findFirstOf<RegionTerritory>(player.location) ?: throw InvalidCommandArgument("You're not in a territory!")

			BazaarItem.findProp(
				and(BazaarItem::seller eq player.slPlayerId, BazaarItem::cityTerritory eq territory.id),
				BazaarItem::itemString
			).toList()
		}
	}

	private fun requireItemInHand(sender: Player): ItemStack {
		val item = sender.inventory.itemInMainHand
		failIf(item.isEmpty) { "You aren't holding an item!" }

		return item
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
		Bazaars.depositListingStock(sender, territory, itemString, limit ?: Int.MAX_VALUE).sendReason(sender)
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

	@Suppress("Unused")
	@Subcommand("list menu|list gui")
	@Description("List the items you're selling at this city")
	fun onListMenu(sender: Player) = asyncCommand(sender) {
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
	@Subcommand("browse")
	@Default
	@Description("Remotely browse city bazaar markets")
	fun onBrowse(sender: Player) {
		BazaarGUIs.openCitySelection(sender, true, null)
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
					val item = fromItemString(itemString)
					return@map guiButton(item) { playerClicker.closeInventory() }
						.setName(item.displayNameComponent)
						.setLore("Price: ${price.toCreditsString()}")
				}.toList()

			sender.openPaginatedMenu("Merchant Prices", items)
		}
	}

	@Subcommand("order create")
	@Description("Create a new buy order at this city")
	@CommandCompletion("@anyItem")
	fun onCreateOrder(sender: Player, itemString: String, quantity: Int, pricePerItem: Double, @Optional priceConfirmation: Double?) = asyncCommand(sender) {
		val territory: RegionTerritory = requireTerritoryIn(sender)
		val realCost = quantity * pricePerItem

		failIf(priceConfirmation != realCost) {
			"You must acknowledge the cost of the listing to create it. The cost is ${realCost.toCreditsString()}. Run the command: /bazaar order create $itemString $quantity $pricePerItem $realCost"
		}

		val result = Bazaars.createOrder(sender, territory, itemString, quantity, pricePerItem)
		result.getReason()?.forEach(sender::sendMessage)
	}
}
