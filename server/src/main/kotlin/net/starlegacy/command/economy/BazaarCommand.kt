package net.starlegacy.command.economy

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Description
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.starlegacy.command.SLCommand
import net.starlegacy.database.schema.economy.BazaarItem
import net.starlegacy.database.schema.economy.CityNPC
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.economy.bazaar.Bazaars
import net.starlegacy.feature.economy.bazaar.Merchants
import net.starlegacy.feature.economy.city.CityNPCs
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.city.TradeCityType
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.space.Sector
import net.starlegacy.feature.space.Space
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.displayName
import net.starlegacy.util.roundToHundredth
import net.starlegacy.util.toCreditsString
import org.bukkit.DyeColor
import org.bukkit.command.CommandSender
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.eq
import kotlin.math.ceil

@CommandAlias("bazaar")
object BazaarCommand : SLCommand() {
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
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Item string of {0}: {1}",
			item.displayName,
			Bazaars.toItemString(item)
		)
	}

	private fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName
		?: "<{Unknown}>" // this will be used if the city is disbanded but their items remain there

	@Subcommand("create")
	@Description("Create a new listing at this city")
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

		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Created listing for {0} at {1}. " +
			"It will not show in the listing until it has some stock. " +
			"To add stock, use /bazaar deposit.", itemString, cityName)
	}

	private fun requireSelling(territory: RegionTerritory, sender: Player, itemString: String) =
		BazaarItem.findOne(BazaarItem.matchQuery(territory.id, sender.slPlayerId, itemString))
			?: fail { "You're not selling $itemString at ${cityName(territory)}" }

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
				sender.sendFeedbackMessage(
					FeedbackType.INFORMATION,
					"Added {0} of {1} to listing in {2}",
					count,
					itemString,
					cityName
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

			sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Withdraw {0} of {1} at {2}" +
					"({3} stack(s) and {4} item(s))", amount, itemString, cityName, fullStacks, remainder)
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
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Removed listing for {0} at {1}",
			itemString,
			cityName
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
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Updated price of {0} at {1} to {2}",
			itemString,
			cityName,
			newPrice.toCreditsString()
		)
	}

	@Suppress("Unused")
	@Subcommand("list")
	@Description("List the items you're selling at this city")
	fun onList(sender: Player) = asyncCommand(sender) {
		val items = BazaarItem.find(BazaarItem::seller eq sender.slPlayerId).toList()
		sender.sendFeedbackMessage(FeedbackType.INFORMATION, "Your Items ({0})", items.size)
		for (item in items) {
			val name = Bazaars.fromItemString(item.itemString).displayName
			val city = cityName(Regions[item.cityTerritory])
			val stock = item.stock
			val uncollected = item.balance.toCreditsString()
			val price = item.price.toCreditsString()
			sender.sendRichMessage( "<aqua>$name <dark_purple>@ <light_purple>$city <dark_gray>[<gray>stock: " +
					"<red>$stock <gray>, balance: <gold>$uncollected<gray>, price: <yellow>$price<dark_gray>]")
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
			sender.sendFeedbackMessage(
				FeedbackType.SUCCESS,
				"Collected {0} from {1} listings",
				total.toCreditsString(),
				count
			)
		}
	}

	@Suppress("Unused")
	@Subcommand("tax")
	@Description("View the tax of the city you're in")
	fun onTax(sender: Player) = asyncCommand(sender) {
		val territory = requireTerritoryIn(sender)
		val city = TradeCities.getIfCity(territory) ?: fail { "You're not in a trade city" }
		sender.sendFeedbackMessage(
			FeedbackType.INFORMATION,
			"Tax of {0}: {1}%",
			city.displayName,
			(city.tax * 100).toInt()
		)
	}

	@Suppress("Unused")
	@Subcommand("browse")
	@Description("Remotely browse city bazaar markets")
	fun onBrowse(sender: Player) {
		val sector = Sector.getSector(sender.world)

		val cities: List<TradeCityData> = CityNPCs.BAZAAR_CITY_TERRITORIES
			.map { Regions.get<RegionTerritory>(it) }
			.filter { Sector.getSector(it.world) == sector }
			.mapNotNull(TradeCities::getIfCity)

		MenuHelper.apply {
			val cityItems = cities.map { city ->
				val territoryId = city.territoryId
				val territory: RegionTerritory = Regions[territoryId]

				// attempt to get the planet icon, just use a detonator if unavailable
				val item: CustomItem = Space.getPlanet(territory.world)?.planetIcon ?: CustomItems.DETONATOR

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
		sender.sendFeedbackMessage(
			FeedbackType.SUCCESS,
			"Bought {0} of {1} for {2} (+ {3} tax)",
			amount,
			item.displayName,
			price.toCreditsString(),
			tax.toCreditsString()
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
						.setName(item.displayName)
						.setLore("Price: ${price.toCreditsString()}")
				}.toList()

			sender.openPaginatedMenu("Merchant Prices", items)
		}
	}
}
