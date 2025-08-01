package net.horizonsend.ion.server.features.economy.cargotrade

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.economy.CargoCrate
import net.horizonsend.ion.common.database.schema.economy.CargoCrateShipment
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.extensions.userErrorAction
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.*
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.cache.trade.CargoCrates
import net.horizonsend.ion.server.features.economy.city.TradeCities
import net.horizonsend.ion.server.features.economy.city.TradeCityData
import net.horizonsend.ion.server.features.economy.city.TradeCityType
import net.horizonsend.ion.server.features.gui.GuiText
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.progression.SLXP
import net.horizonsend.ion.server.features.progression.achievements.Achievement
import net.horizonsend.ion.server.features.progression.achievements.rewardAchievement
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.StarshipType
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.gui.invui.misc.util.input.TextInputMenu.Companion.openInputMenu
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.InputValidator
import net.horizonsend.ion.server.gui.invui.misc.util.input.validator.ValidatorResult
import net.horizonsend.ion.server.gui.invui.utils.buttons.makeGuiButton
import net.horizonsend.ion.server.gui.invui.utils.setTitle
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.*
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.*
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.persistence.PersistentDataType
import org.bukkit.util.Vector
import org.litote.kmongo.eq
import xyz.xenondevs.invui.gui.PagedGui
import xyz.xenondevs.invui.item.impl.AbstractItem
import xyz.xenondevs.invui.window.Window
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ShipmentManager : IonServerComponent() {
	private data class Delivery(
		val id: String,
		val crate: CargoCrate,
		var newDeliveredCrates: Int,
		var oldDeliveredCrates: Int,
		val totalCrates: Int,
		val crateCost: Double,
		val crateRevenue: Double,
		val originCity: TradeCityData,
		val destinationCity: TradeCityData,
		val isReturn: Boolean,
		val routeValue: Double
	)

	override fun onEnable() {
		regenerateShipmentsAsync()
	}

	data class ItemOwnerData(val player: UUID, val dropped: Long)

	/**
	 * A map of crate item entity ID to player.
	 * Expires after an hour.
	 */
	private var crateItemOwnershipMap = mutableMapOf<UUID, ItemOwnerData>()

	// Map of territory id to list of shipments
	private val shipments = ConcurrentHashMap<Oid<Territory>, List<UnclaimedShipment>>()

	/**
	 * Generates new shipments async, then updates the current shipment list with them.
	 * Synchronizes on ShippingShipmentGenerator.
	 */
	fun regenerateShipmentsAsync(callback: () -> Unit = {}) = Tasks.async {
		synchronized(ShipmentGenerator) {
			val generatedShipments = ShipmentGenerator.generateShipmentMap()

			Tasks.sync {
				shipments.clear()
				shipments.putAll(generatedShipments)

				callback()
			}
		}
	}

	/** @return A map of city territory ID to lists of unclaimed shipments */
	fun getShipmentMap(): Map<Oid<Territory>, List<UnclaimedShipment>> = shipments

	private fun getShipments(territoryId: Oid<Territory>): List<UnclaimedShipment> = shipments[territoryId] ?: listOf()

	fun openShipmentSelectMenu(player: Player, cityInfo: TradeCityData) = Tasks.async {
		val gui = PagedGui.items()
			.setStructure(
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
				". . . . . . . . .",
			)
			.build()

		getShipments(cityInfo.territoryId).forEachIndexed { index, shipment: UnclaimedShipment ->
			if (shipment.isAvailable) {
				gui.setItem(index, 1, getCrateMenuItem(shipment, player))
				gui.setItem(index, 2, getPlanetItem(shipment))
			}
		}

		val overlay = GuiText("")
			.add(text("City '${cityInfo.displayName}' Options:"), line = -1)
			.setSlotOverlay(
				"# # # # # # # # #",
				". . . . . . . . .",
				". . . . . . . . .",
				"# # # # # # # # #",
			)
			.build()

			Tasks.sync {
				Window.single()
					.setViewer(player)
					.setGui(gui)
					.setTitle(overlay)
					.build()
					.open()
			}
	}

	private fun getCrateMenuItem(shipment: UnclaimedShipment, player: Player): AbstractItem {
		val item = CrateItems[CargoCrates[shipment.crate]]

		item.updateLore(getCrateItemLore(shipment).map { deserializeComponent(it, legacyAmpersand) })

		return item.makeGuiButton { _, _ ->
			player.closeInventory()
			openAmountPrompt(player, shipment)
		}
	}

	private fun getCrateItemLore(shipment: UnclaimedShipment): List<String> {
		val destinationTerritory: RegionTerritory = Regions[shipment.to.territoryId]
		val destinationWorld = destinationTerritory.world

		return listOf(
			"${SLTextStyle.GRAY}Destination:" +
				" ${SLTextStyle.DARK_GREEN}${shipment.to.displayName}" +
				" on $destinationWorld",
			"${SLTextStyle.GRAY}Cost: ${SLTextStyle.DARK_PURPLE}${shipment.crateCost.toCreditsString()} per crate",
			"${SLTextStyle.GRAY}Days until expiry: ${SLTextStyle.LIGHT_PURPLE}${shipment.expiryDays}",
			aqua("Left click to accept!").bold().toLegacyText()
		)
	}

	private fun getPlanetItem(shipment: UnclaimedShipment): AbstractItem {
		return shipment.to.planetIcon.makeGuiButton { _, _ ->  }
	}

	private fun openAmountPrompt(player: Player, shipment: UnclaimedShipment) {
		val maxCrateCount = StarshipType.entries
			.filter { it.canUse(player) }
			.filter { it.typeCategory == TypeCategory.TRADE_SHIP }
			// Crate limit equation
			.maxOf { (min(0.015 * it.maxSize, sqrt(it.maxSize.toDouble())) * it.crateLimitMultiplier).toInt() }

		val min = balancing.generator.minShipmentSize
		val max = min(balancing.generator.maxShipmentSize, maxCrateCount)

		player.openInputMenu(
			prompt = "Select amount of crates:".toComponent(),
			description = "Between $min and $max".toComponent(),
			inputValidator = InputValidator { result ->
				val amount = result.toIntOrNull() ?: return@InputValidator ValidatorResult.FailureResult(text("Amount must be an integer"))
				if (amount !in min..max) return@InputValidator ValidatorResult.FailureResult(text("Amount must be between $min and $max"))

				ValidatorResult.ValidatorSuccessSingleEntry(amount)
			},
		) { _, result ->
			if (result !is ValidatorResult.ValidatorSuccessSingleEntry) return@openInputMenu

			giveShipment(player, shipment, result.result)
			return@openInputMenu
		}
	}

	private const val TIME_LIMIT = 8L

	private fun giveShipment(player: Player, shipment: UnclaimedShipment, count: Int) {
		val cost = getCost(shipment, count)

		if (!VAULT_ECO.has(player, cost)) {
			player.userError("You can't afford that shipment! It costs ${cost.toCreditsString()}")
			return
		}

		Tasks.async {
			// database stuff async
			val playerId = player.slPlayerId
			if (CargoCrateShipment.hasPurchasedFrom(playerId, shipment.from.territoryId, TIME_LIMIT)) {
				player.userError("You already bought crates from this territory within the past $TIME_LIMIT hours")
				return@async
			}

			if (!shipment.isAvailable) { // someone else might've got it in the process
				return@async player.serverError("Shipment is not available")
			}

			val currentTerritory = Regions.findFirstOf<RegionTerritory>(player.location)
			if (currentTerritory == null) {
				player.serverError("There was an error creating your shipment, please try again.")
				return@async
			}

			val item = runCatching { makeShipmentAndItem(playerId, currentTerritory.id, shipment, count) }
				.getOrElse { exception ->
					exception.printStackTrace()

					player.serverError("There was an error creating your shipment, please try again.")
					return@async
				}

			Tasks.sync {
				if (!shipment.isAvailable) { // someone else might've got it in the process
					return@sync player.serverError("Shipment is not available")
				}
				completePurchase(player, shipment, item, count)
				player.closeInventory()
			}
		}
	}

	private fun getCost(shipment: UnclaimedShipment, count: Int) = shipment.crateCost * count

	private fun completePurchase(player: Player, shipment: UnclaimedShipment, crateItem: ItemStack, count: Int) {
		VAULT_ECO.withdrawPlayer(player, getCost(shipment, count))
		shipment.isAvailable = false
		spawnCrateItems(player, crateItem, count)
		sendAcceptedMessages(shipment, player, count)
	}

	private fun spawnCrateItems(player: Player, crateItem: ItemStack, count: Int) {
		val time = System.currentTimeMillis()
		repeat(count) {
			val entity = player.world.dropItem(player.location, crateItem)
			crateItemOwnershipMap[entity.uniqueId] = ItemOwnerData(player.uniqueId, time)
			entity.isInvulnerable = true
			entity.velocity = Vector(0, 0, 0)
		}
	}

	private fun sendAcceptedMessages(shipment: UnclaimedShipment, player: Player, count: Int) {
		val costString = getCost(shipment, count).toCreditsString()
		val revenueString = (shipment.crateRevenue * count).toCreditsString()
		val planetName = Regions.get<RegionTerritory>(shipment.to.territoryId).world

		player msg "&2Accepted a shipment for &a$costString&2 to deliver &a$count Crates&2 " +
			"to &a${shipment.to.displayName}&2 on &a$planetName&2 " +
			"in exchange for a total revenue of &a$revenueString"

		player msg "&7&oThe items can only be picked up by you for one hour, " +
			"or until you pick them up (and drop them again), so move them to your ship!"
	}

	private fun makeShipmentAndItem(player: SLPlayerId, city: Oid<Territory>, shipment: UnclaimedShipment, count: Int): ItemStack {
		val now = Date(System.currentTimeMillis())
		val expires = Date(now.time + TimeUnit.DAYS.toMillis(shipment.expiryDays.toLong()))
		val crate = CargoCrates[shipment.crate]
		val from = shipment.from.territoryId

		if (from != city) throw IllegalStateException("Unclaimed shipment data didn't match city location!")

		val to = shipment.to.territoryId
		val cost = shipment.crateCost
		val revenue = shipment.crateRevenue

		val id = CargoCrateShipment.create(player, crate._id, now, expires, from, to, count, cost, revenue)
		val crateItem = CrateItems[CargoCrates[shipment.crate]]

		return createBoxedCrateItem(
			crateItem = withShipmentItemId(crateItem, id.toString()),
			shipment = shipment,
			shipmentId = id,
			expires = expires
		)
	}

	/**
	 * Called for when a player clicks an importer NPC.
	 * Goes through all crates in inventory and processes them and sells them.
	 */
	fun onImport(player: Player, city: TradeCityData) {
		/* a set of all the shipment ids present in the items.
		* it is a set rather than a list to limit it to one entry per shipment id */
		val detectedShipments: Set<String> = player.inventory.asSequence().filterNotNull()
			.filter { CargoCrates[it] != null }
			.mapNotNull { getShipmentItemId(it) }
			.toSet()

		if (detectedShipments.isEmpty()) {
			player.userErrorAction("No cargo crates with a mission in your inventory to import!")
			return
		}

		Tasks.async {
			/* go async to fill in shipment info. Doesn't necessarily contain every original shipment. */
			val deliveries: Map<String, Delivery> = fillShipmentInfo(detectedShipments, player, city)

			Tasks.sync {
				/* go sync to give money to player etc */

				if (!player.isOnline) {
					// all we've done is fill info, no harm in gracefully stopping
					return@sync
				}

				val updatedShipments = mutableSetOf<String>() // the shipments that were updated
				var credits = 0.0 // total credits to give to the player in revenue
				var xp = 0.0 // total HEXP to reward the player

				for ((index: Int, item: ItemStack?) in player.inventory.contents.withIndex()) {
					if (item == null) {
						continue
					}

					val shipmentId = getShipmentItemId(item) ?: continue
					val delivery = deliveries[shipmentId] ?: continue

					if (delivery.newDeliveredCrates >= delivery.totalCrates) {
						player.userError("Can't sell more crates than total crates!")
						break
					}

					delivery.newDeliveredCrates++
					updatedShipments += shipmentId

					credits += if (delivery.isReturn) delivery.crateCost else delivery.crateRevenue

					if (!delivery.isReturn) {
						xp += balancing.importExport.baseCrateXP
					}

					player.inventory.setItem(index, null)
				}

				val updatedDeliveries = updatedShipments.mapNotNull { deliveries[it] }

				for (delivery in updatedDeliveries) {
					CargoCrateShipment.addSold(delivery.id, delivery.newDeliveredCrates)
				}

				var totalRevenue = 0.0

				for (delivery in updatedDeliveries) {
					val crate = delivery.crate
					val amountImported = delivery.newDeliveredCrates

					val isReturn = delivery.isReturn
					val crateRevenue = if (isReturn) delivery.crateCost else delivery.crateRevenue
					val revenue = crateRevenue * amountImported
					val revenueString = revenue.toCreditsString()

					val originCityName = delivery.originCity.displayName
					val destinationCityName = delivery.destinationCity.displayName
					player msg "${if (isReturn) "&dReturned" else "&dDelivered"} &b$amountImported " +
						"${crate.color.legacyChatColor}${crate.name} &dCrates " +
						"from &1$originCityName " +
						"&d${if (!isReturn) "to " else "meant for "}&1$destinationCityName " +
						"&dearning &6$revenueString &dfor shipment with ID &3${delivery.id}"

					if (!isReturn) {
						val totalDelivered = delivery.newDeliveredCrates + delivery.oldDeliveredCrates
						val completed = totalDelivered >= delivery.totalCrates

						if (completed) {
							val overallRevenue = crateRevenue * totalDelivered
							player msg "&6Completed shipment (ID: ${delivery.id})! " +
								"Revenue: &e${overallRevenue.toCreditsString()}"

							val cost = delivery.crateCost * delivery.totalCrates
							val profit = overallRevenue - cost

							val costString = cost.toCreditsString()
							val profitString = profit.toCreditsString()
							player msg "&7(Cost was &b$costString&7, so the profit is ~&b$profitString&7)"
						} else {
							player msg "&2Crates Delivered: &a$totalDelivered out of ${delivery.totalCrates}"
							player msg "&2Current Revenue: &a$revenueString " +
								"$7&o(${crateRevenue.toCreditsString()} per crate)"
						}
					}

					totalRevenue += revenue
				}

				xp *= randomDouble(balancing.importExport.minXPFactor, balancing.importExport.maxXPFactor)

				val taxPercent = city.tax

				val tax = (totalRevenue * taxPercent).roundToInt()
				totalRevenue -= tax

				if (xp > 0) {
					SLXP.addAsync(player, xp.roundToInt())
				}

				val playernationid = PlayerCache[player].nationOid

				val capturedStationCount =
					min(CapturableStation.count(CapturableStation::nation eq playernationid).toInt(), 6)
				val siegeBonusPercent = capturedStationCount * 5
				val siegeBonus = totalRevenue * siegeBonusPercent / 100

				player.information("Received $siegeBonusPercent% (C$siegeBonus) bonus from $capturedStationCount captured stations.")

				totalRevenue += siegeBonus

				if (totalRevenue > 0) {
					player msg "&1Revenue from all updated shipments, after tax: " +
						"&b${totalRevenue.toCreditsString()} &8(&c${tax.toCreditsString()} tax&8)"
					VAULT_ECO.depositPlayer(player, totalRevenue)
					giveSettlementProfit(player.name, city, tax)
				}
			}
		}
		player.rewardAchievement(Achievement.COMPLETE_CARGO_RUN)
	}

	private fun giveSettlementProfit(playerName: String, city: TradeCityData, tax: Int) {
		if (city.type != TradeCityType.SETTLEMENT) {
			return
		}

		Tasks.async {
			val territory: RegionTerritory = Regions[city.territoryId]

			val settlementId = territory.settlement
				?: return@async log.warn("Failed to give cut of profit to ${city.displayName}")

			if (tax > 0) {
				Settlement.deposit(settlementId, tax)
				Notify.settlementCrossServer(
					settlementId = settlementId,
					message = miniMessage.deserialize("<gold>Your settlement received <yellow>${tax.toCreditsString()} <gold>from <aqua>$playerName's <gold>completion of a shipment to it.")
				)
			}
		}
	}

	/**
	 * Using the list of shipment ids, fills a map of shipment id to shipment data
	 */
	private fun fillShipmentInfo(
		shipments: Set<String>,
		player: Player,
		city: TradeCityData
	): Map<String, Delivery> {
		return shipments.mapNotNull { id ->
			val shipment: CargoCrateShipment = CargoCrateShipment.getByItemId(id) ?: run {
				log.warn("${player.name} has a crate with shipment id $id, but that id isn't in the database!")
				return@mapNotNull null
			}

			val crateId = shipment.crate
			val crate = CargoCrates[crateId]

			val originTerritoryId = shipment.originTerritory
			val originTerritory: RegionTerritory = Regions[originTerritoryId]
			val from = TradeCities.getIfCity(originTerritory) ?: run {
				player msg red("The city this shipment came from is no longer a city!")
				return@mapNotNull null
			}

			val destinationTerritoryId = shipment.destinationTerritory
			val destinationTerritory: RegionTerritory = Regions[destinationTerritoryId]
			val to = TradeCities.getIfCity(destinationTerritory) ?: run {
				player msg red("The city this shipment was sent to is no longer a city!")
				return@mapNotNull null
			}

			val isReturn = from.territoryId == city.territoryId

			if (to.territoryId != city.territoryId && !isReturn) {
				player msg red("Shipment $id is for city ${to.displayName}, not ${city.displayName}!")
				return@mapNotNull null
			}

			val expires = shipment.expireTime

			if (expires.before(Date.from(Instant.now()))) {
				player msg red("Shipment $id already expired! Expire time: $expires")
				return@mapNotNull null
			}

			val sold = shipment.soldCrates
			val total = shipment.totalCrates
			val cost = shipment.crateCost
			val revenue = shipment.crateRevenue

			val routeValue = ShipmentGenerator.getRouteValue(
				originValue = crate.getValue(originTerritory.world),
				destinationValue = crate.getValue(destinationTerritory.world)
			)

			return@mapNotNull id to Delivery(id, crate, 0, sold, total, cost, revenue, from, to, isReturn, routeValue)
		}.toMap()
	}

	@EventHandler
	fun onPickup(event: EntityPickupItemEvent) {
		val itemOwnerData = crateItemOwnershipMap[event.item.uniqueId] ?: return
		val time = System.currentTimeMillis()
		val entity = event.entity
		if (itemOwnerData.player != entity.uniqueId && time - itemOwnerData.dropped < TimeUnit.HOURS.toMillis(1)) {
			event.isCancelled = true
			if (entity is Player) {
				val playerName = Bukkit.getPlayer(itemOwnerData.player)?.name ?: "an offline player"
				entity action "&cThat crate belongs to $playerName"
			}
		}
	}

	/**
	 * Applies this shipment ID to the provided item, crate or paper inside crate
	 **/
	private fun withShipmentItemId(item: ItemStack, shipmentId: String): ItemStack = item
		.clone()
		.updatePersistentDataContainer {
			set(NamespacedKeys.CARGO_CRATE, PersistentDataType.STRING, shipmentId)
		}

	fun getShipmentItemId(item: ItemStack): String? {
		return item.persistentDataContainer.get(NamespacedKeys.CARGO_CRATE, PersistentDataType.STRING)
	}

	/**
	 * Un box crates that are dropped so that they keep lore
	 */
	@EventHandler
	fun onDrop(event: ItemSpawnEvent) {
		val entity = event.entity
		entity.itemStack = unboxDroppedCrate(entity.itemStack)
	}

	/**
	 * Gives info to players who right click on crates
	 */
	@EventHandler
	fun onCrateClick(event: PlayerInteractEvent) {
		if (event.action != Action.RIGHT_CLICK_BLOCK) {
			return
		}

		val shulkerBox = event.clickedBlock?.state as? ShulkerBox ?: return

		// ignore if it's not a crate
		if (CargoCrates[shulkerBox] == null) {
			return
		}

		val name = shulkerBox.customName() ?: error("No name for shulker box clicked by ${event.player.name}")
        event.player.sendMessage(name)
		val lore: List<String> = shulkerBox.inventory.getItem(0)?.lore ?: return
		lore.forEach(event.player::sendMessage)
	}

	/**
	 * @param crateItem: Shulker box crate item, with a name and persistent data set of the shipment id
	 **/
	private fun createBoxedCrateItem(
		crateItem: ItemStack,
		shipment: UnclaimedShipment,
		shipmentId: Oid<CargoCrateShipment>,
		expires: Date
	): ItemStack {
		val destination: RegionTerritory = Regions[shipment.to.territoryId]
		val originSystemName = Space.planetNameCache[Regions.get<RegionTerritory>(shipment.from.territoryId).world].orNull()?.spaceWorldName
		val destinationSystemName = Space.planetNameCache[destination.world].orNull()?.spaceWorldName

		val lore = mutableListOf(
			ofChildren(text("Shipping From: ", DARK_AQUA), text("${shipment.from.displayName} (${Regions.get<RegionTerritory>(shipment.from.territoryId)}, in system $originSystemName)", AQUA)),
			ofChildren(text("Shipping To: ", DARK_PURPLE), text("${shipment.to.displayName} ($destination), in system $destinationSystemName", GRAY)),
			ofChildren(text("Expires: ", RED), text("$expires", YELLOW)),
			ofChildren(text("Shipment ID: ", DARK_GREEN), text("$shipmentId", GREEN)),
		)

		crateItem.updateLore(lore)

		val crateItemMeta = crateItem.itemMeta as BlockStateMeta
		val shulkerBlockState = crateItemMeta.blockState
		val inventory = (shulkerBlockState as ShulkerBox).inventory

		val basePaperItem = ItemStack(Material.PAPER, 1)
			.updateDisplayName(CargoCrates[shipment.crate].name)
			.updateLore(lore)
			.updatePersistentDataContainer { set(NamespacedKeys.CARGO_CRATE, PersistentDataType.STRING, shipmentId.toString()) }

		val paperItemWithId = withShipmentItemId(basePaperItem, shipmentId.toString())
		inventory.addItem(paperItemWithId)

		shulkerBlockState.persistentDataContainer.set(NamespacedKeys.CARGO_CRATE, PersistentDataType.STRING, shipmentId.toString())

		crateItemMeta.blockState = shulkerBlockState
		crateItem.itemMeta = crateItemMeta

		return crateItem
	}

	private fun unboxDroppedCrate(itemStack: ItemStack): ItemStack {
		val itemMeta = itemStack.itemMeta as? BlockStateMeta ?: return itemStack

		val inventory = (itemMeta.blockState as? ShulkerBox)?.inventory ?: return itemStack

		val firstItem = inventory.getItem(0) ?: return itemStack

		itemStack.lore = firstItem.lore

		val shipmentId = getShipmentItemId(firstItem) ?: run {
			log.warn("Crate had item with no shipment ID")
			return itemStack
		}

		return withShipmentItemId(itemStack, shipmentId)
	}
}
