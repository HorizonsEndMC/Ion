package net.horizonsend.ion.server.features.economy.bazaar

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.BazaarItem
import net.horizonsend.ion.common.database.schema.economy.BazaarOrder
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.utils.input.FutureInputResult
import net.horizonsend.ion.common.utils.input.InputResult
import net.horizonsend.ion.common.utils.input.PotentiallyFutureResult
import net.horizonsend.ion.common.utils.miscellaneous.roundToHundredth
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.formatException
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.gui.invui.bazaar.BazaarGUIs
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openSearchMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.miscellaneous.utils.MATERIALS
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.horizonsend.ion.server.miscellaneous.utils.depositMoney
import net.horizonsend.ion.server.miscellaneous.utils.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.utils.hasEnoughMoney
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.horizonsend.ion.server.miscellaneous.utils.withdrawMoney
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.GREEN
import net.kyori.adventure.text.format.NamedTextColor.RED
import org.bukkit.Location
import org.bukkit.entity.Player
import org.bukkit.event.inventory.ClickType
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gt
import org.litote.kmongo.inc
import org.litote.kmongo.ne
import java.util.function.Consumer
import kotlin.math.roundToInt

object Bazaars : IonServerComponent() {
	val strings = mutableListOf<String>()

	private fun buildStrings() {
		strings.addAll(MATERIALS.filter { it.isItem && !it.isLegacy && !it.isAir }.map { it.name })
		strings.addAll(CustomItemRegistry.identifiers)
		strings.addAll(MultiblockRegistration.getAllMultiblocks().map { "MULTIBLOCK_TOKEN[multiblock=\"${it.javaClass.simpleName}\"]" })
		strings.remove("MULTIBLOCK_TOKEN")
		strings.remove("PACKAGED_MULTIBLOCK")
	}

	fun searchStrings(
		player: Player,
		prompt: Component,
		description: Component,
		backButtonHandler: ((Player) -> Unit)? = null,
		consumer: Consumer<String>
	) {
		Tasks.sync {
			player.openSearchMenu(
				entries = strings,
				searchTermProvider = { string: String -> listOf(string) },
				prompt = prompt,
				description = description,
				backButtonHandler = backButtonHandler,
				componentTransformer = { fromItemString(it).displayNameComponent },
				itemTransformer = { fromItemString(it) },
				handler = { _: ClickType, result: String -> consumer.accept(result) }
			)
		}
	}

	override fun onEnable() {
		buildStrings()
		Tasks.asyncRepeat(20L, 20 * 60 * 60L, /* Every hour */ ::cleanExpiredBazaarEntries)
	}

	fun cleanExpiredBazaarEntries() {

	}

    fun onClickBazaarNPC(player: Player, city: TradeCityData) {
		BazaarGUIs.openCityBrowse(player, city, null)
	}

	fun priceMult(remote: Boolean) = if (remote) 4 else 1

	/**
	 * Checks if the given string is a valid item, and not air.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkHasMoney(player: Player, cost: Number): InputResult {
		if (VAULT_ECO.has(player, cost.toDouble())) return InputResult.InputSuccess

		return InputResult.FailureReason(listOf(template(text("You can't afford that! ({0})", RED), cost.toCreditComponent())))
	}

	/**
	 * Checks if the given string is a valid item, and not air.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkTerritoryPresence(player: Player, territory: RegionTerritory): InputResult {
		if (territory.contains(player.location)) return InputResult.InputSuccess

		return InputResult.FailureReason(listOf(template(text("You must be inside {0} to do that!", RED), cityName(territory))))
	}

	/**
	 * Checks if the given string is a valid item, and not air.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkOrderOwnership(player: Player, order: Oid<BazaarOrder>): InputResult {
		if (BazaarOrder.findOneProp(BazaarOrder::_id eq order, BazaarOrder::player) == player.slPlayerId) return InputResult.InputSuccess

		return InputResult.FailureReason(listOf(text("You don't own that order!", RED)))
	}

	/**
	 * Checks if the given string is a valid item, and not air.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkIsSelling(player: Player, territory: RegionTerritory, itemString: String): ValidatorResult<BazaarItem> {
		val entry = BazaarItem.findOne(BazaarItem.matchQuery(territory.id, player.slPlayerId, itemString))
		if (entry != null) {
			return ValidatorResult.ValidatorSuccessSingleEntry(entry)
		}

		return ValidatorResult.FailureResult(template(text("You're not selling {0} at {1}!", RED), itemString, cityName(territory)))
	}

	/**
	 * Checks if the player has an order for the item string at the provided city
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkHasOrder(player: SLPlayerId, territory: RegionTerritory, itemString: String): ValidatorResult<BazaarOrder> {
		val entry = BazaarOrder.findOne(and(BazaarOrder::player eq player, BazaarOrder::cityTerritory eq territory.id, BazaarOrder::itemString eq itemString))
		if (entry != null) {
			return ValidatorResult.ValidatorSuccessSingleEntry(entry)
		}

		val name = SLPlayer.getName(player)

		return ValidatorResult.FailureResult(template(text("{0} doesn't have an order for {1} at {2}!", RED), name, itemString, cityName(territory)))
	}

	/**
	 * Checks if the given string is a valid item, and not air.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkValidString(itemString: String): ValidatorResult<ItemStack> {
		try {
			val itemStack = fromItemString(itemString)

			if (!itemStack.type.isItem || itemStack.isEmpty) return ValidatorResult.FailureResult(template(text("Invalid item string {0}! Empty items are not allowed.", RED), itemString))

			return ValidatorResult.ValidatorSuccessSingleEntry(itemStack)
		} catch (e: Exception) {
			e.printStackTrace()
			return ValidatorResult.FailureResult(template(text("Invalid item string {0}! To see an item's string, use /bazaar string.", RED), itemString))
		}
	}

	/**
	 * Checks if the player is in combat.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkEconomyEnabled(): InputResult {
		if (!ConfigurationFiles.featureFlags().economy) {
			return InputResult.FailureReason(listOf(text("Economy is disabled on this server!", RED)))
		}
		return InputResult.InputSuccess
	}

	/**
	 * Checks if the player is in combat.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkCombatTag(player: Player): InputResult {
		if (CombatTimer.isNpcCombatTagged(player) || CombatTimer.isPvpCombatTagged(player)) {
			return InputResult.FailureReason(listOf(text("Bazaars cann't be used while in combat!", RED)))
		}
		return InputResult.InputSuccess
	}

	/**
	 * Checks if the price is greater than zero, and doesn't go beyond 2 decimal places.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkValidPrice(price: Double): InputResult {
		if (price <= 0) return InputResult.FailureReason(listOf(template(text("Invalid Unit Price {0}! Amount must be greater than 0.", RED), price)))
		if (price != price.roundToHundredth()) return InputResult.FailureReason(listOf(template(text("Invalid Unit Price {0}! Unit price cannot go further than 2 decimal places.", RED), price)))

		return InputResult.InputSuccess
	}

	/**
	 * Checks if the quantity is above zero.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkValidQuantity(quantity: Int): InputResult {
		if (quantity <= 0) return InputResult.FailureReason(listOf(text("Amount must be at least 1!", RED)))

		return InputResult.InputSuccess
	}

	/**
	 * Checks if a territory is valid for bazaar activity.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkValidTerritory(territory: RegionTerritory): ValidatorResult<TradeCityData> {
		val cityData = TradeCities.getIfCity(territory) ?: return ValidatorResult.FailureResult(text("Territory is not a trade city", RED))

		if (!CityNPCs.BAZAAR_CITY_TERRITORIES.contains(territory.id)) {
			return ValidatorResult.FailureResult(text("City doesn't have a registered bazaar", RED))
		}

		return ValidatorResult.ValidatorSuccessSingleEntry(cityData)
	}

	/**
	 * Checks if a territory is valid for bazaar activity.
	 * Returns a success, or failure result with a reason.
	 **/
	fun checkInValidCity(player: Player): ValidatorResult<TradeCityData> {
		val territory = Regions.findFirstOf<RegionTerritory>(player.location) ?: return ValidatorResult.FailureResult(text("You're not in a territory!", RED))

		val cityData = TradeCities.getIfCity(territory) ?: return ValidatorResult.FailureResult(text("Territory is not a trade city", RED))

		if (!CityNPCs.BAZAAR_CITY_TERRITORIES.contains(territory.id)) {
			return ValidatorResult.FailureResult(text("City doesn't have a registered bazaar", RED))
		}

		return ValidatorResult.ValidatorSuccessSingleEntry(cityData)
	}

	/**
	 * Creates a bazaar sell listing
	 * Returns a success, or failure result with a reason.
	 **/
	fun createListing(player: Player, territory: RegionTerritory, itemString: String, pricePerItem: Double): InputResult {
		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val territoryResult = checkValidTerritory(territory)
		if (!territoryResult.isSuccess()) return territoryResult

		val priceResult = checkValidPrice(pricePerItem)
		if (!priceResult.isSuccess()) return priceResult

		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		val cityName = cityName(territory)

		if (!BazaarItem.none(BazaarItem.matchQuery(territory.id, player.slPlayerId, itemString))) {
			return InputResult.FailureReason(listOf(text("You're already selling $itemString at $cityName!", RED)))
		}

		BazaarItem.create(territory.id, player.slPlayerId, itemString, pricePerItem)

		return InputResult.SuccessReason(listOf(
			template(text("Created a listing for {0} at {1}", GREEN), itemString, cityName),
			text("It will not show in the listing until it has some stock. To add stock, use /bazaar deposit.", GREEN)
		))
	}

	/**
	 * Removes all matching items from the player's inventory and deposits them into the bazaar listing.
	 * Returns a success, or failure result with a reason.
	 **/
	fun depositListingStock(player: Player, inventory: Inventory, territory: RegionTerritory, itemString: String, limit: Int): PotentiallyFutureResult {
		val cityName = cityName(territory)

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val itemValidationResult = checkValidString(itemString)
		val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		val result = FutureInputResult()

		Tasks.sync {
			val count = takePlayerItemsOfType(inventory, itemReference, limit)

			if (count == 0) {
				result.complete(InputResult.FailureReason(listOf(template(text("You do not have any {0} to deposit!", RED), itemString))))
				return@sync
			}

			Tasks.async {
				BazaarItem.addStock(resultItem._id, count)
				player.information("Added $count of $itemString to listing in $cityName")

				result.complete(InputResult.SuccessReason(listOf(template(text("Added {0} of {1} to listing in {2}", GREEN), count, itemString, cityName))))
			}
		}

		return result
	}

	fun withdrawListingBalance(player: Player, territory: RegionTerritory, itemString: String, amount: Int): InputResult {
		val cityName = cityName(territory)

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val itemValidationResult = checkValidString(itemString)
		val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

		val amountResult = checkValidQuantity(amount)
		if (!amountResult.isSuccess()) return amountResult

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		if (resultItem.stock < amount) {
			return InputResult.FailureReason(listOf(
				template(text("Your listing of {0} at {1} only has {2} item(s) in stock", RED), itemString, cityName, resultItem.stock)
			))
		}

		BazaarItem.removeStock(resultItem._id, amount)

		Tasks.sync {
			val (fullStacks, remainder) = giveOrDropItems(itemReference, amount, player)

			player.sendMessage(template(text("Withdrew {0} of {1} at {2} ({3} stack(s) and {4} item(s)", GREEN), amount, itemString, cityName, fullStacks, remainder))
		}

		return InputResult.InputSuccess
	}

	fun removeListing(player: Player, territory: RegionTerritory, itemString: String): InputResult {
		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		return removeListing(player, resultItem)
	}

	fun removeListing(player: Player, order: BazaarItem): InputResult {
		val territory = Regions.get<RegionTerritory>(order.cityTerritory)
		val itemString = order.itemString

		val cityName = cityName(territory)

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val itemValidationResult = checkValidString(itemString)
		if (!itemValidationResult.isSuccess()) return itemValidationResult

		if (order.stock > 0) {
			return InputResult.FailureReason(listOf(
				template(text("Withdraw all items before removing! (/bazaar withdraw {0} {1})", RED), useQuotesAroundObjects = false, itemString, order.stock)
			))
		}

		BazaarItem.delete(order._id)

		return InputResult.SuccessReason(listOf(
			template(text("Removed listing for {0} at {1}", GREEN), itemString, cityName)
		))
	}

	fun setListingPrice(player: Player, territory: RegionTerritory, itemString: String, newPrice: Double): InputResult {
		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val territoryResult = checkValidTerritory(territory)
		if (!territoryResult.isSuccess()) return territoryResult

		val priceResult = checkValidPrice(newPrice)
		if (!priceResult.isSuccess()) return priceResult

		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		BazaarItem.setPrice(resultItem._id, newPrice)

		return InputResult.SuccessReason(listOf(
			template(text("Updated price of {0} at {1} to {2}", GREEN), itemString, cityName(territory), newPrice.toCreditsString())
		))
	}

	fun collectListingProfit(player: Player): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val senderId = player.slPlayerId
		val total = BazaarItem.collectMoney(senderId)
		val count = BazaarItem.count(BazaarItem::seller eq senderId)

		Tasks.sync {
			VAULT_ECO.depositPlayer(player, total)
		}

		return InputResult.SuccessReason(listOf(template(text("Collected {0} from {1} listings.", GREEN), total.toCreditComponent(), count)))
	}

	/**
	 * @param itemConsumer is a function that returns a function. It is run async, then the returned function is run sync. This allows async setup then a sync execution.
	 **/
	fun tryBuyFromSellOrder(player: Player, item: BazaarItem, amount: Int, remote: Boolean, itemConsumer: (ItemStack, Int, Double, Int) -> (() -> InputResult)): PotentiallyFutureResult {
		val price: Double = item.price
		val revenue: Double = amount * price
		val priceMult = priceMult(remote)
		val cost: Double = revenue * priceMult

		val moneyCheck = checkHasMoney(player, cost)
		if (!moneyCheck.isSuccess()) return moneyCheck

		val cityResult = checkValidTerritory(Regions[item.cityTerritory])
		val cityData = cityResult.result ?: return cityResult

		val futureResult = FutureInputResult()

		Tasks.async {
			if (!BazaarItem.hasStock(item._id, amount)) {
				futureResult.complete(InputResult.FailureReason(listOf(text("Item no longer has $amount in stock", RED))))
				return@async
			}

			if (BazaarItem.matches(item._id, BazaarItem::price ne price)) {
				futureResult.complete(InputResult.FailureReason(listOf(text("Price has changed", RED))))
				return@async
			}

			val itemStack = fromItemString(item.itemString)

			BazaarItem.removeStock(item._id, amount)
			item.stock -= amount

			val tax = (cityData.tax * revenue).roundToInt()
			BazaarItem.depositMoney(item._id, revenue - tax)
			if (cityData.type == TradeCityType.SETTLEMENT) {
				Settlement.deposit(cityData.settlementId, tax)
			}

			// Runs setup that creates a function to be invoked sync
			val syncBlock = itemConsumer.invoke(itemStack, amount, cost, priceMult)

			Tasks.sync {
				VAULT_ECO.withdrawPlayer(player, cost)
				futureResult.complete(syncBlock.invoke())
			}
		}

		return futureResult
	}

	/**
	 * Creates a bazaar order
	 * Returns a success, or failure result with a reason.
	 **/
	fun createOrder(player: Player, territory: RegionTerritory, itemString: String, orderQuantity: Int, individualPrice: Double): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val territoryResult = checkValidTerritory(territory)
		if (!territoryResult.isSuccess()) return territoryResult

		val priceResult = checkValidPrice(individualPrice)
		if (!priceResult.isSuccess()) return priceResult

		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		val cityName = cityName(territory)
		val totalPrice = orderQuantity * individualPrice

		if (BazaarOrder.any(and(BazaarOrder::player eq player.slPlayerId, BazaarOrder::itemString eq itemString, BazaarOrder::cityTerritory eq territory.id))) {
			return InputResult.FailureReason(listOf(template(text("You already have an order for {0} at {1}!", RED), itemString, cityName)))
		}

		if (!player.hasEnoughMoney(totalPrice)) {
			return InputResult.FailureReason(listOf(text("You don't have enough money to create that order!", RED)))
		}

		try {
			player.withdrawMoney(totalPrice)
			BazaarOrder.create(player.slPlayerId, territory.id, itemString, orderQuantity, individualPrice)
			player.information("Created a bazaar order for $orderQuantity of $itemString for $individualPrice per item at $cityName for $totalPrice.")

			return InputResult.SuccessReason(listOf(
				text("Created a bazaar order for $orderQuantity of $itemString for $individualPrice per item at $cityName for $totalPrice.", GREEN)
			))
		} catch (e: Throwable) {
			return InputResult.FailureReason(listOf(
				text("There was an error adding your order. Please forward this to staff.", RED),
				formatException(e)
			))
		}
	}

	fun deleteOrder(player: Player, order: Oid<BazaarOrder>): InputResult {
		val ownershipCheck = checkOrderOwnership(player, order)
		if (!ownershipCheck.isSuccess()) return ownershipCheck

		if (BazaarOrder.matches(order, BazaarOrder::stock gt 0)) {
			return InputResult.FailureReason(listOf(text("Your order still has unwithdrawn stock! Withdraw it all first!", RED)))
		}

		val remainingBalance = BazaarOrder.findPropById(order, BazaarOrder::balance)

		BazaarOrder.delete(order)

		if (remainingBalance != null) {
			Tasks.sync {
				player.depositMoney(remainingBalance)
			}
		}

		return InputResult.SuccessReason(listOf(
			text("Deleted bazaar order.", GREEN),
			template(text("You have been refunded {0} credits, for products that had not been fulfilled", GREEN), remainingBalance?.toCreditComponent())
		))
	}

//	fun editOrderQuantity(player: Player, order: Oid<BazaarOrder>): InputResult {
//		val ownershipCheck = checkOrderOwnership(player, order)
//		if (!ownershipCheck.isSuccess()) return ownershipCheck
//
//
//	}
//
//	fun editOrderPrice(player: Player, order: Oid<BazaarOrder>): InputResult {
//		val ownershipCheck = checkOrderOwnership(player, order)
//		if (!ownershipCheck.isSuccess()) return ownershipCheck
//	}

	fun withdrawOrderStock(player: Player, order: Oid<BazaarOrder>, limit: Int): PotentiallyFutureResult {
		val ownershipCheck = checkOrderOwnership(player, order)
		if (!ownershipCheck.isSuccess()) return ownershipCheck

		val orderDocument = BazaarOrder.findById(order) ?: return InputResult.FailureReason(listOf(text("That order does not exist!", RED)))

		val currentStock = orderDocument.stock

		if (currentStock == 0) {
			return InputResult.FailureReason(listOf(text("That order does have any stock to withdraw!", RED)))
		}

		val toRemove = minOf(limit, currentStock)

		BazaarOrder.updateById(order, inc(BazaarOrder::stock, -toRemove))
		orderDocument.stock -= toRemove

		val item = fromItemString(orderDocument.itemString)

		val future = FutureInputResult()

		Tasks.sync {
			val (fullStacks, remainder) = giveOrDropItems(item, toRemove, player)

			future.complete(
				InputResult.SuccessReason(listOf(template(
				text("Withdrew {0} of {1} at {2} ({3} stack(s) and {4} item(s) items from the balance. {5} remain.", GREEN),
				toRemove,
				orderDocument.itemString,
				cityName(Regions[orderDocument.cityTerritory]),
				fullStacks,
				remainder,
				orderDocument.stock
			))))
		}

		return future
	}

	fun fulfillOrder(fulfiller: Player, order: Oid<BazaarOrder>, limit: Int): PotentiallyFutureResult {
		if (limit < 1) return InputResult.FailureReason(listOf(text("Limit must be greater than 0!", RED)))

		val combatResult = checkCombatTag(fulfiller)
		if (!combatResult.isSuccess()) return combatResult

		val orderDocument = BazaarOrder.findById(order) ?: return InputResult.FailureReason(listOf(text("That order does not exist!", RED)))

		val territoryResult = checkValidTerritory(Regions[orderDocument.cityTerritory])
		if (!territoryResult.isSuccess()) return territoryResult

		val itemValidationResult = checkValidString(orderDocument.itemString)
		val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

		val result = FutureInputResult()

		Tasks.sync {
			val count = takePlayerItemsOfType(fulfiller.inventory, itemReference, limit)

			if (count == 0) {
				result.complete(
					InputResult.FailureReason(listOf(
					template(text("You do not have any {0} to fulfill the order with!", RED), itemReference.displayNameComponent)
				)))

				return@sync
			}

			Tasks.async {
				val profit = BazaarOrder.fulfillStock(order, fulfiller.slPlayerId, count)
				val ordererName = SLPlayer.getName(orderDocument.player)

				fulfiller.depositMoney(profit)

				result.complete(
					InputResult.SuccessReason(listOf(
					template(text("Fulfilled {0} of {1}'s order of {2} for a profit of {3}", GREEN), count, ordererName, itemReference.displayNameComponent, profit.toCreditComponent())
				)))

				//TODO logic for removing the order
			}
		}

		return result
	}

	// START UTILS

	/** Formats the trade city name of the given territory */
	fun cityName(territory: RegionTerritory) = TradeCities.getIfCity(territory)?.displayName ?: "<{Unknown}>" // this will be used if the city is disbanded but their items remain there

	/**
	 * Gives the player items, or drops them at their location if their inventory is full.
	 *
	 * Returns a pair of full stacks of items to the remainder
	 **/
	fun giveOrDropItems(itemStack: ItemStack, amount: Int, sender: Player): Pair<Int, Int> {
		return giveOrDropItems(itemStack, amount, sender.inventory, sender.eyeLocation)
	}

	fun giveOrDropItems(itemStack: ItemStack, amount: Int, inventory: Inventory, location: Location): Pair<Int, Int> {
		val maxStackSize = itemStack.maxStackSize
		val fullStacks = amount / maxStackSize

		fun add(amount: Int) {
			val stack = itemStack.clone().apply { this.amount = amount }
			val remainder: HashMap<Int, ItemStack> = inventory.addItem(stack)

			// remainder is when the inventory didn't have space

			for (remainingItem in remainder.values) {
				location.world.dropItem(location, remainingItem)
			}
		}

		repeat(fullStacks) { add(maxStackSize) }
		val remainder = amount % maxStackSize
		if (remainder > 0) {
			add(remainder)
		}
		return Pair(fullStacks, remainder)
	}

	/**
	 * Removes items matching the provided reference from the players inventory
	 * Returns the count of items removed
	 *
	 * @param limit the limit to take
	 **/
	fun takePlayerItemsOfType(inventory: Inventory, itemReference: ItemStack, limit: Int): Int {
		var remaining = limit
		var count = 0

		for ((index, itemStack) in inventory.withIndex()) {
			if (itemStack?.isSimilar(itemReference) == true) {
				val toTake = minOf(remaining, itemStack.amount)

				count += toTake
				remaining -= toTake

				if (itemStack.amount == toTake) {
					inventory.setItem(index, null)
				} else {
					itemStack.amount -= toTake
				}
			}
		}

		return count
	}

	fun bulkDepositToSellOrders(player: Player, items: Map<String, ArrayDeque<ItemReference>>): PotentiallyFutureResult {
		val territoryResult = checkInValidCity(player)
		val territory = territoryResult.result ?: return territoryResult

		val futureResult = FutureInputResult()

		Tasks.async {
			val soldItems = BazaarItem
				.find(and(BazaarItem::seller eq player.slPlayerId, BazaarItem::cityTerritory eq territory.territoryId))
				.map(BazaarItem::itemString)

			val bareResults = mutableListOf<Component>()
			val futureResults = mutableListOf<InputResult>()

			for ((itemString, references) in items) {
				if (!soldItems.contains(itemString)) {
					bareResults.add(template(text("You're not selling {0} at {1}", RED), itemString, territory.displayName))
					continue
				}

				for ((inventory, _) in references.groupBy { it.inventory }) {
					// Need to run get to halt the thread until the transaction is completed. Otherwise, there will be write conflicts since the
					// db write in this function is async
					futureResults.add(depositListingStock(player, inventory, Regions[territory.territoryId], itemString, Integer.MAX_VALUE).get())
				}
			}

			val fullLore = bareResults + futureResults.mapNotNull { it.getReason() }.flatten()

			if (futureResults.any { it.isSuccess() }) futureResult.complete(InputResult.SuccessReason(fullLore))
			else futureResult.complete(InputResult.FailureReason(fullLore))
		}

		return futureResult
	}
}
