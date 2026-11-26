package net.horizonsend.ion.server.features.economy.bazaar

import com.mongodb.MongoWriteException
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
import net.horizonsend.ion.server.command.GlobalCompletions.fromItemString
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarBuyFromSellOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCollectItemFromBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCollectMoneyFromSellOrdersEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDepositItemToBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDepositItemToSellOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarCreateBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarDeleteBuyOrderEvent
import net.horizonsend.ion.server.features.economy.bazaar.event.BazaarWithdrawItemFromSellOrderEvent
import net.horizonsend.ion.server.features.economy.city.CityNPCs
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.multiblock.MultiblockRegistration
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.transport.items.util.ItemReference
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.SpaceRegion
import net.horizonsend.ion.server.features.world.WorldFlag
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
import org.litote.kmongo.inc
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer
import kotlin.math.roundToInt

object Bazaars : IonServerComponent() {
	val strings = mutableListOf<String>()

	private fun buildStrings() {
		strings.addAll(MATERIALS.filter { it.isItem && !it.isLegacy && !it.isAir }.map { it.name })
		strings.addAll(CustomItemKeys.allStrings())
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
		//Tasks.asyncDelay(20L, ::migrateEntries)
	}

	fun cleanExpiredBazaarEntries() {

	}

	// REMOVE AFTER RUNNING ONCE
	/*
	fun migrateEntries() {
		val replacements = mutableMapOf(
			"CHAIN" to "IRON_CHAIN"
		)

		for (entry in BazaarItem.all()) {
			val replacement = replacements[entry.itemString] ?: continue
			BazaarItem.updateById(entry._id, org.litote.kmongo.setValue(BazaarItem::itemString, replacement))
		}

		for (entry in BazaarOrder.all()) {
			val replacement = replacements[entry.itemString] ?: continue
			BazaarOrder.updateById(entry._id, org.litote.kmongo.setValue(BazaarOrder::itemString, replacement))
		}
	}
	 */

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
	 * Checks if the [player] is within [territory], and returns a validator result.
	 **/
	fun checkInsideCorrectTerritory(player: Player, territory: RegionTerritory): ValidatorResult<RegionTerritory> {
		val currentTerritory = Regions.findFirstOf<RegionTerritory>(player.location) ?: return ValidatorResult.FailureResult(text("You're not in a territory!", RED))

		if (territory != currentTerritory) return ValidatorResult.FailureResult(text("You're not in the correct territory!", RED))

		return ValidatorResult.ValidatorSuccessSingleEntry(territory)
	}

	/**
	 * Creates a bazaar sell listing
	 * Returns a success, or failure result with a reason.
	 **/
	fun createListing(player: Player, territory: RegionTerritory, itemString: String, pricePerItem: Double): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val territoryResult = checkValidTerritory(territory)
		if (!territoryResult.isSuccess()) return territoryResult

		val priceResult = checkValidPrice(pricePerItem)
		if (!priceResult.isSuccess()) return priceResult

		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		val cityName = cityName(territory)
		val alreadySelling = InputResult.FailureReason(listOf(text("You're already selling $itemString at $cityName!", RED)))

		if (!BazaarItem.none(BazaarItem.matchQuery(territory.id, player.slPlayerId, itemString))) {
			return alreadySelling
		}

		try {
			BazaarItem.create(territory.id, player.slPlayerId, itemString, pricePerItem)
		} catch (_: IllegalArgumentException) {
			return alreadySelling
		} catch (exception: MongoWriteException) {
			if (exception.error.code != 11000) throw exception
			return alreadySelling
		}

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
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val cityName = cityName(territory)

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val itemValidationResult = checkValidString(itemString)
		val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult
		val key = BazaarGuard.listingKey(resultItem._id)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val completion = GuardedResult(BazaarGuard.Release(key))
		val transfer = InventoryTransfer(player, inventory, itemReference, "Bazaar listing ${resultItem._id} deposit", completion)

		transfer.take(limit, { template(text("You do not have any {0} to deposit!", RED), itemString) }) { count ->
			commitListingDeposit(transfer, resultItem._id, itemString, cityName, count)
		}
		return completion.future
	}

	private fun commitListingDeposit(transfer: InventoryTransfer, listing: Oid<BazaarItem>, itemString: String, cityName: String, count: Int) {
		val committed = attempt("Failed to add items to Bazaar listing $listing") {
			BazaarItem.addStock(listing, count)
		}.getOrElse {
			transfer.returnItems(count, "There was an error adding your items to the listing")
			return
		}
		if (!committed) {
			transfer.returnItems(count, "That listing no longer exists")
			return
		}

		val success = attempt("Failed to format the deposit result for listing $listing") {
			InputResult.SuccessReason(listOf(template(text("Added {0} of {1} to listing in {2}", GREEN), count, itemString, cityName)))
		}.getOrDefault(InputResult.InputSuccess)
		val finish = { transfer.completion.complete(success) }
		scheduleStage(Tasks::sync, "Failed to schedule the Bazaar deposit event for listing $listing", finish) {
			attempt("Failed to call the Bazaar deposit event for listing $listing") {
				BazaarDepositItemToSellOrderEvent(transfer.player, itemString, count, cityName).callEvent()
			}
			finish()
		}
	}

	fun withdrawListingBalance(player: Player, territory: RegionTerritory, itemString: String, amount: Int): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val cityName = cityName(territory)

		val combatResult = checkCombatTag(player)
		if (!combatResult.isSuccess()) return combatResult

		val itemValidationResult = checkValidString(itemString)
		val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

		val amountResult = checkValidQuantity(amount)
		if (!amountResult.isSuccess()) return amountResult

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		val key = BazaarGuard.listingKey(resultItem._id)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val release = BazaarGuard.Release(key)

		try {
			if (!BazaarItem.removeStock(resultItem._id, amount)) {
				val currentStock = BazaarItem.findOnePropById(resultItem._id, BazaarItem::stock) ?: 0
				release()
				return InputResult.FailureReason(listOf(
					template(text("Your listing of {0} at {1} only has {2} item(s) in stock", RED), itemString, cityName, currentStock)
				))
			}

			Tasks.sync {
				try {
					val (fullStacks, remainder) = giveOrDropItems(itemReference, amount, player)

					player.sendMessage(template(text("Withdrew {0} of {1} at {2} ({3} stack(s) and {4} item(s)", GREEN), amount, itemString, cityName, fullStacks, remainder))
					BazaarWithdrawItemFromSellOrderEvent(player, itemString, amount, cityName).callEvent()
				} finally {
					release()
				}
			}
		} catch (exception: Throwable) {
			release()
			throw exception
		}

		return InputResult.InputSuccess
	}

	fun removeListing(player: Player, territory: RegionTerritory, itemString: String): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val itemResult = checkIsSelling(player, territory, itemString)
		val resultItem = itemResult.result ?: return itemResult

		return removeListing(player, resultItem)
	}

	fun removeListing(player: Player, order: BazaarItem): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

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

		if (!BazaarItem.deleteIfEmpty(order._id)) {
			val currentListing = BazaarItem.findById(order._id)
				?: return InputResult.FailureReason(listOf(text("That listing does not exist!", RED)))

			return InputResult.FailureReason(listOf(
				template(text("Withdraw all items before removing! (/bazaar withdraw {0} {1})", RED), useQuotesAroundObjects = false, itemString, currentListing.stock)
			))
		}

		return InputResult.SuccessReason(listOf(
			template(text("Removed listing for {0} at {1}", GREEN), itemString, cityName)
		))
	}

	fun setListingPrice(player: Player, territory: RegionTerritory, itemString: String, newPrice: Double): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

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
		val key = BazaarGuard.playerKey(player.uniqueId)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val release = BazaarGuard.Release(key)

		val total: Double
		val count: Long
		try {
			total = BazaarItem.collectMoney(senderId)
			count = BazaarItem.count(BazaarItem::seller eq senderId)

			Tasks.sync {
				try {
					VAULT_ECO.depositPlayer(player, total)
					BazaarCollectMoneyFromSellOrdersEvent(player, total.toInt()).callEvent()
				} finally {
					release()
				}
			}
		} catch (exception: Throwable) {
			release()
			throw exception
		}

		return InputResult.SuccessReason(listOf(template(text("Collected {0} from {1} listings.", GREEN), total.toCreditComponent(), count)))
	}

	/**
	 * @param itemConsumer is a function that returns a function. It is run async, then the returned function is run sync. This allows async setup then a sync execution.
	 **/
	fun tryBuyFromSellOrder(player: Player, item: BazaarItem, amount: Int, remote: Boolean, itemConsumer: (ItemStack, Int, Double, Int) -> (() -> InputResult)): PotentiallyFutureResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		if (remote) {
			val cityWorld = Regions.get<RegionTerritory>(item.cityTerritory).bukkitWorld

			if (cityWorld != null) {
				val buyerInDeepSpace = player.world.hasFlag(WorldFlag.DOMINION_WORLD)
				val buyerInCore = player.world.hasFlag(WorldFlag.CORE_REGION_WORLD)
				val cityIsTradeWorld = cityWorld.hasFlag(WorldFlag.DOMINION_TRADE_WORLD)
				val cityInCore = cityWorld.hasFlag(WorldFlag.CORE_REGION_WORLD)

				if (buyerInDeepSpace && cityInCore) return InputResult.FailureReason(listOf(text("You cannot remotely purchase from core region cities while in deep space!", RED)))
				if (buyerInCore && cityIsTradeWorld) return InputResult.FailureReason(listOf(text("You cannot remotely purchase from deep space trade cities!", RED)))

				val buyerRegion = player.world.ion.getSpaceRegion()
				val cityRegion = cityWorld.ion.getSpaceRegion()
				if (buyerRegion != cityRegion && buyerRegion != SpaceRegion.NONE && cityRegion != SpaceRegion.NONE) {
					return InputResult.FailureReason(listOf(text("You cannot remotely purchase from a different region!", RED)))
				}
			}
		}
		return SellOrderPurchase(player, item, amount, remote, itemConsumer).start()
	}

	private class SellOrderPurchase(
		private val player: Player,
		private val item: BazaarItem,
		private val amount: Int,
		private val remote: Boolean,
		private val itemConsumer: (ItemStack, Int, Double, Int) -> (() -> InputResult),
	) {
		private val price = item.price
		private val revenue = amount * price
		private val multiplier = priceMult(remote)
		private val cost = revenue * multiplier
		private val purchaseFailure = "There was an error completing your purchase; staff have been notified."
		private val compensationFailure = "There was an error reversing your purchase; staff have been notified."

		private data class PurchaseTax(val city: TradeCityData, val amount: Int, val deposited: Boolean)

		fun start(): PotentiallyFutureResult {
			val key = BazaarGuard.playerKey(player.uniqueId)
			if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
			val release = BazaarGuard.Release(key)
			var handedOff = false

			try {
				val moneyCheck = checkHasMoney(player, cost)
				if (!moneyCheck.isSuccess()) return moneyCheck

				val cityResult = checkValidTerritory(Regions[item.cityTerritory])
				val city = cityResult.result ?: return cityResult
				val completion = GuardedResult(release)

				scheduleStage(Tasks::async, "Failed to schedule a Bazaar purchase from listing ${item._id}", { completion.fail(purchaseFailure) }) {
					reserveStock(city, completion)
				}
				handedOff = true
				return completion.future
			} finally {
				if (!handedOff) release()
			}
		}

		private fun reserveStock(city: TradeCityData, completion: GuardedResult) {
			val tax = attempt("Failed to calculate tax for Bazaar purchase from listing ${item._id}") {
				(city.tax * revenue).roundToInt()
			}.getOrElse {
				completion.fail(purchaseFailure)
				return
			}
			val reserved = attempt("Failed to purchase from Bazaar listing ${item._id}") {
				BazaarItem.purchase(item._id, amount, price, revenue - tax)
			}.getOrElse {
				completion.fail(purchaseFailure)
				return
			}
			if (!reserved) {
				val finish = { completion.fail("Item no longer has $amount in stock, or the price changed") }
				scheduleStage(Tasks::sync, "Failed to schedule the Bazaar purchase result for listing ${item._id}", finish, finish)
				return
			}

			prepareDelivery(city, tax, completion)
		}

		private fun prepareDelivery(city: TradeCityData, tax: Int, completion: GuardedResult) {
			val collectTax = city.type == TradeCityType.SETTLEMENT
			val taxDeposit = attempt("Failed to deposit tax for Bazaar purchase from listing ${item._id}") {
				if (collectTax) Settlement.deposit(city.settlementId, tax)
			}
			if (taxDeposit.isFailure) {
				compensate(PurchaseTax(city, tax, deposited = false), completion, purchaseFailure)
				return
			}
			val purchaseTax = PurchaseTax(city, tax, deposited = collectTax)
			val deliver = attempt("Failed to prepare items for Bazaar purchase from listing ${item._id}") {
				itemConsumer(fromItemString(item.itemString), amount, cost, multiplier)
			}.getOrElse {
				compensate(purchaseTax, completion, "There was an error preparing your purchase; staff have been notified.")
				return
			}

			val onRejected = { compensate(purchaseTax, completion, purchaseFailure) }
			scheduleStage(Tasks::sync, "Failed to schedule item delivery for Bazaar listing ${item._id}", onRejected) {
				chargeAndDeliver(purchaseTax, completion, deliver)
			}
		}

		private fun chargeAndDeliver(tax: PurchaseTax, completion: GuardedResult, deliver: () -> InputResult) {
			val charged = attempt("Failed to charge buyer for Bazaar listing ${item._id}") {
				VAULT_ECO.withdrawPlayer(player, cost).transactionSuccess()
			}.getOrElse {
				compensate(tax, completion, purchaseFailure)
				return
			}
			if (!charged) {
				compensate(tax, completion, "Payment could not be withdrawn; your purchase was reversed.")
				return
			}

			val result = attempt("Failed to complete Bazaar purchase from listing ${item._id}") {
				BazaarBuyFromSellOrderEvent(player, item, amount, revenue, tax.amount.toDouble(), cost, remote).callEvent()
				deliver()
			}.getOrElse {
				compensate(tax, completion, purchaseFailure, refundBuyer = true)
				return
			}
			completion.complete(result)
		}

		private fun compensate(tax: PurchaseTax, completion: GuardedResult, reason: String, refundBuyer: Boolean = false) {
			scheduleStage(Tasks::async, "Failed to schedule Bazaar purchase compensation for listing ${item._id}", { completion.fail(compensationFailure) }) {
				val reversed = attempt("Failed to reverse Bazaar purchase for listing ${item._id}") {
					check(BazaarItem.reversePurchase(item._id, amount, revenue - tax.amount)) { "Bazaar listing no longer exists" }
					if (tax.deposited) Settlement.withdraw(tax.city.settlementId, tax.amount)
				}.isSuccess
				finishCompensation(completion, if (reversed) reason else compensationFailure, refundBuyer)
			}
		}

		private fun finishCompensation(completion: GuardedResult, reason: String, refundBuyer: Boolean) {
			val onRejected = { completion.fail(if (refundBuyer) compensationFailure else reason) }
			scheduleStage(Tasks::sync, "Failed to schedule Bazaar purchase refund for listing ${item._id}", onRejected) {
				val refunded = !refundBuyer || attempt("Failed to refund $cost after Bazaar purchase from listing ${item._id} failed") {
					val refund = VAULT_ECO.depositPlayer(player, cost)
					check(refund.transactionSuccess()) { refund.errorMessage }
				}.isSuccess
				completion.fail(if (refunded) reason else compensationFailure)
			}
		}
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

		val quanityResult =checkValidQuantity(orderQuantity)
		if(!quanityResult.isSuccess()) return quanityResult

		val stringResult = checkValidString(itemString)
		if (!stringResult.isSuccess()) return stringResult

		val cityName = cityName(territory)
		val totalPrice = orderQuantity * individualPrice
		val key = "createOrder:${player.slPlayerId}:$itemString:${territory.id}"
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY

		try {
			if (BazaarOrder.any(and(BazaarOrder::player eq player.slPlayerId, BazaarOrder::itemString eq itemString, BazaarOrder::cityTerritory eq territory.id))) {
				return InputResult.FailureReason(listOf(template(text("You already have an order for {0} at {1}!", RED), itemString, cityName)))
			}

			if (!player.hasEnoughMoney(totalPrice)) {
				return InputResult.FailureReason(listOf(text("You don't have enough money to create that order!", RED)))
			}

			player.withdrawMoney(totalPrice)
			try {
				BazaarOrder.create(player.slPlayerId, territory.id, itemString, orderQuantity, individualPrice)
			} catch (exception: Throwable) {
				try {
					player.depositMoney(totalPrice)
				} catch (refundException: Throwable) {
					exception.addSuppressed(refundException)
					IonServer.slF4JLogger.error("Failed to refund $totalPrice after creating a Bazaar order failed", refundException)
				}
				throw exception
			}

			player.information("Created a bazaar order for $orderQuantity of $itemString for $individualPrice per item at $cityName for $totalPrice.")

			Tasks.sync {
				BazaarCreateBuyOrderEvent(player, itemString, orderQuantity, totalPrice, cityName).callEvent()
			}

			return InputResult.SuccessReason(listOf(
				text("Created a bazaar order for $orderQuantity of $itemString for $individualPrice per item at $cityName for $totalPrice.", GREEN)
			))
		} catch (e: Throwable) {
			return InputResult.FailureReason(listOf(
				text("There was an error adding your order. Please forward this to staff.", RED),
				formatException(e)
			))
		} finally {
			BazaarGuard.release(key)
		}
	}

	fun deleteOrder(player: Player, order: Oid<BazaarOrder>): InputResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val key = BazaarGuard.orderKey(order)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val release = BazaarGuard.Release(key)
		var handedOff = false

		try {
			val ownershipCheck = checkOrderOwnership(player, order)
			if (!ownershipCheck.isSuccess()) return ownershipCheck

			val deletedOrder = BazaarOrder.deleteIfEmpty(order)
			if (deletedOrder == null) {
				val currentOrder = BazaarOrder.findById(order)
				return if (currentOrder == null) {
					InputResult.FailureReason(listOf(text("That order does not exist!", RED)))
				} else {
					InputResult.FailureReason(listOf(text("Your order still has unwithdrawn stock! Withdraw it all first!", RED)))
				}
			}

			val remainingBalance = deletedOrder.balance
			Tasks.sync {
				try {
					player.depositMoney(remainingBalance)
					BazaarDeleteBuyOrderEvent(player, remainingBalance).callEvent()
				} finally {
					release()
				}
			}
			handedOff = true

			return InputResult.SuccessReason(listOf(
				text("Deleted bazaar order.", GREEN),
				template(text("You have been refunded {0} credits, for products that had not been fulfilled", GREEN), remainingBalance.toCreditComponent())
			))
		} finally {
			if (!handedOff) release()
		}
	}

	fun withdrawOrderStock(player: Player, order: Oid<BazaarOrder>, limit: Int): PotentiallyFutureResult {
		return withdrawOrderStock(player, order, limit) { item, orderDocument, toRemove ->
			{
				val (fullStacks, remainder) = giveOrDropItems(item, toRemove, player)

				InputResult.SuccessReason(listOf(template(
					text("Withdrew {0} of {1} at {2} ({3} stack(s) and {4} item(s) items from the balance. {5} remain.", GREEN),
					toRemove,
					orderDocument.itemString,
					cityName(Regions[orderDocument.cityTerritory]),
					fullStacks,
					remainder,
					orderDocument.stock
				)))
			}
		}
	}

	fun withdrawOrderStock(player: Player, order: Oid<BazaarOrder>, limit: Int, itemConsumer: (ItemStack, BazaarOrder, Int) -> (() -> InputResult)): PotentiallyFutureResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		val key = BazaarGuard.orderKey(order)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val release = BazaarGuard.Release(key)
		var handedOff = false

		try {
			val ownershipCheck = checkOrderOwnership(player, order)
			if (!ownershipCheck.isSuccess()) return ownershipCheck

			var orderDocument: BazaarOrder? = null
			var toRemove = 0
			var attempts = 0
			while (orderDocument == null && attempts < 3) {
				attempts++
				val currentOrder = BazaarOrder.findById(order)
					?: return InputResult.FailureReason(listOf(text("That order does not exist!", RED)))
				if (currentOrder.stock <= 0) {
					return InputResult.FailureReason(listOf(text("That order does have any stock to withdraw!", RED)))
				}

				val withinTerritoryResult = checkInsideCorrectTerritory(player, Regions[currentOrder.cityTerritory])
				if (!withinTerritoryResult.isSuccess()) return withinTerritoryResult

				val amount = minOf(limit, currentOrder.stock)
				if (amount <= 0) return InputResult.FailureReason(listOf(text("There are no items available to withdraw!", RED)))

				if (!BazaarOrder.collectItemBalance(order, amount)) continue

				currentOrder.stock -= amount
				orderDocument = currentOrder
				toRemove = amount
			}

			val updatedOrder = orderDocument
				?: return InputResult.FailureReason(listOf(text("The order stock changed; please try again.", RED)))

			val completion = GuardedResult(release)
			deliverWithdrawnStock(player, updatedOrder, toRemove, itemConsumer, completion)
			handedOff = true
			return completion.future
		} finally {
			if (!handedOff) release()
		}
	}

	private fun deliverWithdrawnStock(player: Player, order: BazaarOrder, amount: Int, itemConsumer: (ItemStack, BazaarOrder, Int) -> (() -> InputResult), completion: GuardedResult) {
		val deliver = attempt("Failed to prepare items withdrawn from Bazaar order ${order._id}") {
			itemConsumer(fromItemString(order.itemString), order, amount)
		}.getOrElse {
			restoreWithdrawnStock(order._id, amount, completion, "There was an error preparing the withdrawn items.", completeOnMainThread = false)
			return
		}

		val onDeliveryFailure = {
			restoreWithdrawnStock(order._id, amount, completion, "There was an error giving the withdrawn items.", completeOnMainThread = true)
		}
		scheduleStage(Tasks::sync, "Failed to schedule item delivery for Bazaar order ${order._id}", onDeliveryFailure) delivery@{
			val result = attempt("Failed to give items withdrawn from Bazaar order ${order._id}", deliver).getOrElse {
				onDeliveryFailure()
				return@delivery
			}
			attempt("Failed to call the Bazaar withdrawal event for order ${order._id}") {
				BazaarCollectItemFromBuyOrderEvent(player, order.itemString, amount, cityName(Regions[order.cityTerritory])).callEvent()
			}
			completion.complete(result)
		}
	}

	private fun restoreWithdrawnStock(order: Oid<BazaarOrder>, amount: Int, completion: GuardedResult, reason: String, completeOnMainThread: Boolean) {
		val restoreFailed = "There was an error restoring the withdrawn stock; staff have been notified."
		scheduleStage(Tasks::async, "Failed to schedule stock restoration for Bazaar order $order", { completion.fail(restoreFailed) }) {
			val restored = attempt("Failed to restore stock after withdrawing from Bazaar order $order") {
				val update = BazaarOrder.updateById(order, inc(BazaarOrder::stock, amount))
				check(update.matchedCount == 1L) { "Bazaar order no longer exists" }
			}.isSuccess
			val finish = { completion.fail(if (restored) reason else restoreFailed) }
			if (completeOnMainThread) scheduleStage(Tasks::sync, "Failed to schedule the Bazaar withdrawal result for order $order", finish, finish)
			else finish()
		}
	}

	fun fulfillOrder(fulfiller: Player, inventory: Inventory, order: Oid<BazaarOrder>, limit: Int, consumedAmountConsumer: Consumer<Int> = Consumer {  }): PotentiallyFutureResult {
		val economyCheck = checkEconomyEnabled()
		if (!economyCheck.isSuccess()) return economyCheck

		if (limit < 1) return InputResult.FailureReason(listOf(text("Limit must be greater than 0!", RED)))

		val combatResult = checkCombatTag(fulfiller)
		if (!combatResult.isSuccess()) return combatResult

		val key = BazaarGuard.orderKey(order)
		if (!BazaarGuard.tryAcquire(key)) return BazaarGuard.BUSY
		val release = BazaarGuard.Release(key)
		var handedOff = false

		try {
			val orderDocument = BazaarOrder.findById(order)
				?: return InputResult.FailureReason(listOf(text("That order does not exist!", RED)))

			val territoryResult = checkValidTerritory(Regions[orderDocument.cityTerritory])
			if (!territoryResult.isSuccess()) return territoryResult

			val withinTerritoryResult = checkInsideCorrectTerritory(fulfiller, Regions[orderDocument.cityTerritory])
			if (!withinTerritoryResult.isSuccess()) return withinTerritoryResult

			val itemValidationResult = checkValidString(orderDocument.itemString)
			val itemReference: ItemStack = itemValidationResult.result ?: return itemValidationResult

			val cityData = territoryResult.result ?: return territoryResult
			val completion = GuardedResult(release)
			val transfer = InventoryTransfer(fulfiller, inventory, itemReference, "Bazaar order $order fulfillment", completion)
			val toTake = minOf(orderDocument.requestedQuantity - orderDocument.fulfilledQuantity, limit)
			transfer.take(toTake, { template(text("You do not have any {0} to fulfill the order with!", RED), itemReference.displayNameComponent) }) { count ->
				commitOrderFulfillment(transfer, orderDocument, cityData, count, consumedAmountConsumer)
			}

			handedOff = true
			return completion.future
		} finally {
			if (!handedOff) release()
		}
	}

	private fun commitOrderFulfillment(transfer: InventoryTransfer, order: BazaarOrder, city: TradeCityData, count: Int, consumedAmountConsumer: Consumer<Int>) {
		val revenue = attempt("Failed to fulfill Bazaar order ${order._id}") {
			BazaarOrder.fulfillStock(order._id, transfer.player.slPlayerId, count, order.pricePerItem)
		}.getOrElse {
			transfer.returnItems(count, "There was an error fulfilling the order")
			return
		}
		if (revenue == null) {
			transfer.returnItems(count, "The order changed while you were fulfilling it")
			return
		}

		payForFulfillment(transfer, order, city, count, revenue, consumedAmountConsumer)
	}

	private fun payForFulfillment(transfer: InventoryTransfer, order: BazaarOrder, city: TradeCityData, count: Int, revenue: Double, consumedAmountConsumer: Consumer<Int>) {
		val tax: Int
		val profit: Double
		val success = try {
			tax = (city.tax * revenue).roundToInt()
			profit = revenue - tax
			val ordererName = SLPlayer.getName(order.player)
			transfer.player.depositMoney(profit)
			consumedAmountConsumer.accept(count)
			InputResult.SuccessReason(listOf(
				template(text("Fulfilled {0} of {1}'s order of {2} for a profit of {3} (revenue {4} minus tax {5})", GREEN), count, ordererName, transfer.item.displayNameComponent, profit.toCreditComponent(), revenue.toCreditComponent(), tax.toCreditComponent())
			))
		} catch (exception: Throwable) {
			IonServer.slF4JLogger.error("Bazaar order ${order._id} was fulfilled, but payout or follow-up processing failed", exception)
			transfer.completion.fail("The order was fulfilled, but a follow-up action failed; staff have been notified.")
			return
		}

		transfer.completion.complete(success)
		attempt("Failed to schedule the Bazaar fulfillment event for order ${order._id}") {
			Tasks.sync {
				attempt("Failed to call the Bazaar fulfillment event for order ${order._id}") {
					BazaarDepositItemToBuyOrderEvent(transfer.player, order, count, revenue, tax.toDouble(), profit, city.displayName).callEvent()
				}
			}
		}
	}

	// START UTILS

	private class GuardedResult(private val release: BazaarGuard.Release) {
		val future = FutureInputResult()

		fun complete(result: InputResult) {
			// Completion can run callbacks inline, including ones that acquire the same guard.
			release()
			future.complete(result)
		}

		fun fail(message: String) = complete(InputResult.FailureReason(listOf(text(message, RED))))
	}

	private inline fun <T> attempt(failure: String, action: () -> T): Result<T> =
		runCatching(action).onFailure { IonServer.slF4JLogger.error(failure, it) }

	private fun scheduleStage(schedule: (() -> Unit) -> Unit, failure: String, onRejected: () -> Unit, stage: () -> Unit) {
		if (attempt(failure) { schedule(stage) }.isFailure) onRejected()
	}

	private class InventoryTransfer(
		val player: Player,
		private val inventory: Inventory,
		val item: ItemStack,
		private val operation: String,
		val completion: GuardedResult,
	) {
		fun take(limit: Int, noItems: () -> Component, commit: (Int) -> Unit) {
			val preparationFailure = "There was an error preparing your items"
			scheduleStage(Tasks::sync, "Failed to schedule item removal for $operation", { completion.fail("$preparationFailure.") }) remove@{
				val count = attempt("Failed to remove items for $operation") {
					takePlayerItemsOfType(inventory, item, limit)
				}.getOrElse {
					completion.fail("$preparationFailure.")
					return@remove
				}
				if (count == 0) {
					val message = attempt("Failed to format the missing items result for $operation", noItems)
						.getOrElse { text("There are no matching items.", RED) }
					completion.complete(InputResult.FailureReason(listOf(message)))
					return@remove
				}

				scheduleStage(Tasks::async, "Failed to schedule the database write for $operation", { restoreItems(count, preparationFailure) }) {
					commit(count)
				}
			}
		}

		fun returnItems(count: Int, reason: String) {
			scheduleStage(Tasks::sync, "Failed to schedule item return for $operation", { reportReturnFailure(reason) }) {
				restoreItems(count, reason)
			}
		}

		private fun restoreItems(count: Int, reason: String) {
			val restored = attempt("Failed to return $count items for $operation") {
				giveOrDropItems(item, count, inventory, player.eyeLocation)
			}.isSuccess
			if (restored) completion.fail("$reason; your items were returned.")
			else reportReturnFailure(reason)
		}

		private fun reportReturnFailure(reason: String) = completion.fail("$reason; we couldn't return all of your items. Staff have been notified.")
	}

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

			itemsLoop@ for ((itemString, references) in items) {
				if (!soldItems.contains(itemString)) {
					bareResults.add(template(text("You're not selling {0} at {1}", RED), itemString, territory.displayName))
					continue
				}

				for ((inventory, _) in references.groupBy { it.inventory }) {
					// This loop runs on a worker thread; wait for each async inventory update before continuing.
					val depositResult = depositListingStock(player, inventory, Regions[territory.territoryId], itemString, Integer.MAX_VALUE).get()
					futureResults.add(depositResult)
					if (depositResult === BazaarGuard.BUSY) break@itemsLoop
				}
			}

			val fullLore = bareResults + futureResults.mapNotNull { it.getReason() }.flatten()

			if (futureResults.any { it.isSuccess() }) futureResult.complete(InputResult.SuccessReason(fullLore))
			else futureResult.complete(InputResult.FailureReason(fullLore))
		}

		return futureResult
	}

	fun bulkFulfillOrder(player: Player, order: Oid<BazaarOrder>, references: Collection<ItemReference>, limit: Int, consumedAmountConsumer: Consumer<Int> = Consumer { }): PotentiallyFutureResult {
		val territoryResult = checkInValidCity(player)
		if (!territoryResult.isSuccess()) return territoryResult

		if (references.isEmpty()) {
			return InputResult.FailureReason(listOf(
				template(text("You do not have any items to fulfill the order with!", RED))
			))
		}

		val futureResult = FutureInputResult()

		Tasks.async {
			val bareResults = mutableListOf<Component>()
			val futureResults = mutableListOf<InputResult>()

			var remaining = limit
			val totalConsumed = AtomicInteger()

			for ((inventory, items) in references.groupBy { it.inventory }) {
				if (remaining <= 0) break
				if (items.isEmpty()) continue

				// This loop runs on a worker thread; wait for each async inventory update before continuing.
				val fulfillmentResult = fulfillOrder(player, inventory, order, remaining) {
					remaining -= it
					totalConsumed.addAndGet(it)
				}.get()
				futureResults.add(fulfillmentResult)
				if (fulfillmentResult === BazaarGuard.BUSY) break
			}

			val fullLore = bareResults + futureResults.mapNotNull { it.getReason() }.flatten()
			val consumed = totalConsumed.get()
			if (consumed > 0) {
				try {
					consumedAmountConsumer.accept(consumed)
				} catch (exception: Throwable) {
					IonServer.slF4JLogger.error("Failed to report consumed Bazaar order fulfillment amount", exception)
				}
			}

			if (futureResults.any { it.isSuccess() }) futureResult.complete(InputResult.SuccessReason(fullLore))
			else futureResult.complete(InputResult.FailureReason(fullLore))
		}

		return futureResult
	}
}
