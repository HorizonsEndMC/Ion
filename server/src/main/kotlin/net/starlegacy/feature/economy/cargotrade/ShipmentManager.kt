package net.starlegacy.feature.economy.cargotrade

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import net.horizonsend.ion.common.database.enums.Achievement
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.features.achievements.rewardAchievement
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.cache.trade.CargoCrates
import net.starlegacy.database.Oid
import net.starlegacy.database.schema.economy.CargoCrate
import net.starlegacy.database.schema.economy.CargoCrateShipment
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.schema.nations.CapturableStation
import net.starlegacy.database.schema.nations.Settlement
import net.starlegacy.database.schema.nations.Territory
import net.starlegacy.database.slPlayerId
import net.starlegacy.feature.economy.city.TradeCities
import net.starlegacy.feature.economy.city.TradeCityData
import net.starlegacy.feature.economy.city.TradeCityType
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.nations.gui.input
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionTerritory
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.feature.space.Space
import net.starlegacy.feature.starship.StarshipType
import net.starlegacy.feature.starship.StarshipType.PLATFORM
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Notify
import net.starlegacy.util.SLTextStyle
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.action
import net.starlegacy.util.aqua
import net.starlegacy.util.bold
import net.starlegacy.util.colorize
import net.starlegacy.util.getNBTInt
import net.starlegacy.util.getNBTString
import net.starlegacy.util.msg
import net.starlegacy.util.orNull
import net.starlegacy.util.randomDouble
import net.starlegacy.util.red
import net.starlegacy.util.setDisplayNameAndGet
import net.starlegacy.util.setLoreAndGet
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withNBTString
import net.starlegacy.util.yellow
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.block.ShulkerBox
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.block.Action
import org.bukkit.event.entity.EntityPickupItemEvent
import org.bukkit.event.entity.ItemSpawnEvent
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.inventory.meta.BlockStateMeta
import org.bukkit.util.Vector
import org.litote.kmongo.eq
import java.time.Instant
import java.util.*
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit
import kotlin.collections.set
import kotlin.math.min
import kotlin.math.roundToInt
import kotlin.math.sqrt

object ShipmentManager : SLComponent() {
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

	fun openShipmentSelectMenu(player: Player, cityInfo: TradeCityData) {
		MenuHelper.apply {
			val pane = staticPane(0, 0, 9, 2)

			getShipments(cityInfo.territoryId).forEachIndexed { index, shipment: UnclaimedShipment ->
				if (shipment.isAvailable) {
					pane.addItem(getCrateItem(shipment, player), index, 0)
					pane.addItem(getPlanetItem(shipment), index, 1)
				}
			}

			gui(pane.height, "&lCity '${cityInfo.displayName}' Options:".colorize())
				.withPane(pane)
				.show(player)
		}
	}

	private fun MenuHelper.getCrateItem(shipment: UnclaimedShipment, player: Player): GuiItem {
		val item = CrateItems[CargoCrates[shipment.crate]]
		return guiButton(item) {
			whoClicked.closeInventory()
			openAmountPrompt(player, shipment)
		}.setLore(getCrateItemLore(shipment))
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

	private fun MenuHelper.getPlanetItem(shipment: UnclaimedShipment): GuiItem {
		val destinationTerritory: RegionTerritory = Regions[shipment.to.territoryId]
		val destinationWorld = destinationTerritory.world
		val planetId = destinationWorld.lowercase(Locale.getDefault()).replace(" ", "")
		val planetIcon = CustomItems["planet_icon_$planetId"] ?: CustomItems.DETONATOR
		return guiButton(planetIcon.itemStack(1))
	}

	private fun openAmountPrompt(player: Player, shipment: UnclaimedShipment) {
		player.input("Select amount of crates:") { _: Player, answer ->
			val digit = answer.filter { it.isDigit() }
			val amount = digit.toIntOrNull() ?: return@input "Amount must be an integer"

			val playerMaxShipSize =
				StarshipType.values().filter { !it.isWarship && it.canUse(player) && it != PLATFORM }
					.sortedByDescending { it.maxSize }[0].maxSize

			val min = balancing.generator.minShipmentSize
			val max = min(
				balancing.generator.maxShipmentSize,
				(min(0.015 * playerMaxShipSize, sqrt(playerMaxShipSize.toDouble()))).toInt()
			)
			if (amount !in min..max) {
				return@input "Amount must be between $min and $max"
			}

			giveShipment(player, shipment, amount)
			return@input null
		}
	}

	private const val TIME_LIMIT = 8L

	private fun giveShipment(player: Player, shipment: UnclaimedShipment, count: Int) {
		val cost = getCost(shipment, count)
		if (!VAULT_ECO.has(player, cost)) {
			return player msg red("You can't afford that shipment! It costs ${cost.toCreditsString()}")
		}
		Tasks.async {
			// database stuff async
			val playerId = player.slPlayerId
			if (CargoCrateShipment.hasPurchasedFrom(playerId, shipment.from.territoryId, TIME_LIMIT)) {
				player msg red("You already bought crates from this territory within the past $TIME_LIMIT hours")
				return@async
			}
			val item = makeShipmentAndItem(playerId, shipment, count)
			Tasks.sync {
				if (!shipment.isAvailable) { // someone else might've got it in the process
					return@sync player msg red("Shipment is not available")
				}
				completePurchase(player, shipment, item, count)
			}
		}
	}

	private fun getCost(shipment: UnclaimedShipment, count: Int) = shipment.crateCost * count

	private fun completePurchase(player: Player, shipment: UnclaimedShipment, item: ItemStack, count: Int) {
		VAULT_ECO.withdrawPlayer(player, getCost(shipment, count))
		shipment.isAvailable = false
		spawnCrateItems(player, item, count)
		sendAcceptedMessages(shipment, player, count)
	}

	private fun spawnCrateItems(player: Player, item: ItemStack, count: Int) {
		val time = System.currentTimeMillis()
		repeat(count) {
			val entity = player.world.dropItem(player.location, item)
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

	private fun makeShipmentAndItem(player: SLPlayerId, shipment: UnclaimedShipment, count: Int): ItemStack {
		val now = Date(System.currentTimeMillis())
		val expires = Date(now.time + TimeUnit.DAYS.toMillis(shipment.expiryDays.toLong()))
		val crate = CargoCrates[shipment.crate]
		val from = shipment.from.territoryId
		val to = shipment.to.territoryId
		val cost = shipment.crateCost
		val revenue = shipment.crateRevenue

		val id = CargoCrateShipment.create(player, crate._id, now, expires, from, to, count, cost, revenue)
		val crateItem = CrateItems[CargoCrates[shipment.crate]]
		return createBoxedCrateItem(withShipmentItemId(crateItem, id.toString()), shipment, id, expires)
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
			player action yellow("No cargo crates with a mission in your inventory to import!")
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
				var xp = 0.0 // total SLXP to reward the player

				for ((index: Int, item: ItemStack?) in player.inventory.contents!!.withIndex()) {
					if (item == null) {
						continue
					}

					val shipmentId = getShipmentItemId(item) ?: continue
					val delivery = deliveries[shipmentId] ?: continue

					if (delivery.newDeliveredCrates >= delivery.totalCrates) {
						player msg "&cCan't sell more crates than total crates!"
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
						"${crate.color.chatColor}${crate.name} &dCrates " +
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

				val playernationid = PlayerCache[player].nation

				val capturedStationCount =
					min(CapturableStation.count(CapturableStation::nation eq playernationid).toInt(), 6)
				val siegeBonusPercent = capturedStationCount * 5
				val siegeBonus = totalRevenue * siegeBonusPercent / 100

				player.information(
					"Received $siegeBonusPercent% (C$siegeBonus) bonus from $capturedStationCount captured stations."
				)

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
				Notify.settlement(
					settlementId = settlementId,
					message = "&6Your settlement received &e${tax.toCreditsString()} from $playerName's completion of a shipment to it."
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

	private fun withShipmentItemId(itemStack: ItemStack, shipmentId: String): ItemStack {
		return itemStack.withNBTString("shipment_oid", shipmentId)
	}

	fun getShipmentItemId(item: ItemStack): String? {
		return item.getNBTString("shipment_oid") ?: item.getNBTInt("shipment_id")?.toString()
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

		val name = shulkerBox.customName ?: error("No name for shulker box clicked by ${event.player.name}")
		event.player msg name
		val lore: List<String> = shulkerBox.inventory.getItem(0)?.lore ?: return
		lore.forEach(event.player::sendMessage)
	}

	private fun createBoxedCrateItem(
		itemStack: ItemStack,
		shipment: UnclaimedShipment,
		shipmentId: Oid<CargoCrateShipment>,
		expires: Date
	): ItemStack {
		val destination: RegionTerritory = Regions[shipment.to.territoryId]
		val systemName = Space.planetNameCache[destination.world].orNull()?.spaceWorldName

		val lore = listOf(
			"&3Shipping From: &b${shipment.from.displayName} (${Regions.get<RegionTerritory>(shipment.from.territoryId)}, in system $systemName)",
			"&5Shipping To: &d${shipment.to.displayName}&7 ($destination)",
			"&cExpires: &e$expires",
			"&2Shipment ID: &a$shipmentId"/*,
			"&6Shipment Route Value: &e${shipment.routeValue.roundToHundredth()}"*/
		).map(String::colorize)
		itemStack.lore = lore

		val itemMeta = itemStack.itemMeta as BlockStateMeta
		val blockState = itemMeta.blockState
		val inventory = (blockState as InventoryHolder).inventory

		val baseItem = ItemStack(Material.PAPER, 1)
			.setDisplayNameAndGet(CargoCrates[shipment.crate].name)
			.setLoreAndGet(lore)

		val containerItem = withShipmentItemId(baseItem, shipmentId.toString())
		inventory.addItem(containerItem)

		itemMeta.blockState = blockState
		itemStack.itemMeta = itemMeta
		return itemStack
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
