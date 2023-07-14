package net.starlegacy.feature.economy.collectors

import com.github.stefvanschie.inventoryframework.gui.GuiItem
import com.github.stefvanschie.inventoryframework.pane.OutlinePane
import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import net.citizensnpcs.api.event.NPCLeftClickEvent
import net.citizensnpcs.api.event.NPCRightClickEvent
import net.horizonsend.ion.common.extensions.userError
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.Style
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.SLComponent
import net.starlegacy.cache.trade.EcoStations
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.economy.CollectedItem
import net.horizonsend.ion.server.database.schema.economy.EcoStation
import net.starlegacy.feature.misc.CustomItem
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.nations.gui.playerClicker
import net.starlegacy.feature.progression.SLXP
import net.starlegacy.sharedDataFolder
import net.starlegacy.util.MenuHelper
import net.starlegacy.util.Tasks
import net.starlegacy.util.VAULT_ECO
import net.starlegacy.util.colorize
import net.horizonsend.ion.server.miscellaneous.displayNameComponent
import net.horizonsend.ion.server.miscellaneous.displayNameString
import net.starlegacy.util.loadConfig
import net.starlegacy.util.msg
import net.starlegacy.util.randomRange
import net.starlegacy.util.toCreditsString
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.inventory.ItemStack
import org.litote.kmongo.inc
import kotlin.math.roundToInt
import kotlin.math.sqrt

object CollectionMissions : SLComponent() {
	data class Config(val generateAmount: Int = 27, val xpPerCreditRoot: Double = 0.5, val buyMultiplier: Double = 2.0)

	private lateinit var config: Config

	override fun onEnable() {
		rebalance()
	}

	@EventHandler
	fun onNPCRightClick(event: NPCRightClickEvent) {
		val ecoStation: Oid<EcoStation> = Collectors.getCollectorStation(event.npc) ?: return
		openSellMenu(event.clicker, EcoStations[ecoStation])
	}

	@EventHandler
	fun onNPCLeftClick(event: NPCLeftClickEvent) {
		val ecoStation: Oid<EcoStation> = Collectors.getCollectorStation(event.npc) ?: return
		openBuyMenu(event.clicker, EcoStations[ecoStation])
	}

	private fun createItem(collectedItem: CollectedItem): ItemStack = getItemFromString(collectedItem.itemString)
		?: error("Failed to parse item string ${collectedItem.itemString}")

	fun getItemFromString(itemString: String): ItemStack? {
		CustomItems[itemString]?.let { return it.itemStack(1) }
		val material: Material = Material.getMaterial(itemString) ?: return null
		return ItemStack(material, 1)
	}

	fun getString(itemStack: ItemStack): String {
		return CustomItems[itemStack]?.id ?: itemStack.type.toString()
	}

	private val itemCache: LoadingCache<Oid<EcoStation>, List<CollectedItem>> = CacheBuilder.newBuilder()
		.build(
			CacheLoader.from { stationId ->
				requireNotNull(stationId)
				return@from CollectedItem.findAllAt(stationId).toList()
			}
		)

	private val missions: LoadingCache<Oid<EcoStation>, MutableList<CollectionMission>> = CacheBuilder.newBuilder()
		.build(
			CacheLoader.from { stationId ->
				val station = EcoStations[requireNotNull(stationId)]
				return@from (1..config.generateAmount).asSequence().map { generateMission(station) }.toMutableList()
			}
		)

	fun rebalance() {
		config = loadConfig(sharedDataFolder, "collection_missions")
		reset()
	}

	fun reset() {
		for (cache in arrayOf(missions, itemCache)) {
			cache.invalidateAll()
			cache.cleanUp()
		}
	}

	private fun generateMission(station: EcoStation, item: CollectedItem = randomItem(station)): CollectionMission {
		val stacks: Int = randomRange(item.minStacks, item.maxStacks)
		val reward: Int = (item.value * stacks).roundToInt()
		val xp: Int = (config.xpPerCreditRoot * sqrt(item.value)).roundToInt() * stacks

		return CollectionMission(item, stacks, reward, xp)
	}

	private fun randomItem(station: EcoStation) = itemCache[station._id].random()

	private fun openSellMenu(player: Player, ecoStation: EcoStation) {
		val stationId = ecoStation._id
		val available: List<CollectionMission> = missions[stationId]

		check(available.size <= config.generateAmount) {
			"Should never be more than ${config.generateAmount} missions, but at ${ecoStation.name} there are ${available.size}"
		}

		val rows: Int = config.generateAmount / 9

		MenuHelper.apply {
			val pane: OutlinePane = outlinePane(x = 0, y = 0, length = 9, height = rows)

			for (mission: CollectionMission in available) {
				val item: GuiItem = getMissionItem(mission, ecoStation)
				pane.addItem(item)
			}

			gui(rows, title = "${ecoStation.name} Collection Missions").withPane(pane).show(player)
		}
	}

	private fun MenuHelper.getMissionItem(mission: CollectionMission, ecoStation: EcoStation): GuiItem {
		val itemStack: ItemStack = createItem(mission.item)

		itemStack.amount = mission.stacks

		val stacks: Int = mission.stacks
		val itemName: String = (itemStack.displayNameComponent as TextComponent).content()
		val rewardCredits: String = mission.reward.toCreditsString()
		val xp: Int = mission.xp

		itemStack.lore = listOf(
			"&3Eco Station&7:&b ${ecoStation.name}".colorize(),
			"&2Collect &a$stacks&2 &nstacks&2 of &f$itemName&2 in return for &6$rewardCredits&2 and &5$xp SLXP&2.".colorize(),
			"&7&oTo turn the items in, click on this mission icon!".colorize()
		)

		return guiButton(itemStack) {
			onClickMissionButton(playerClicker, ecoStation, mission)
		}
	}

	private fun onClickMissionButton(clicker: Player, ecoStation: EcoStation, mission: CollectionMission) {
		clicker.closeInventory()
		tryCompleteMission(clicker, ecoStation._id, mission)
		openSellMenu(clicker, ecoStation) // re-open menu
	}

	private fun getBuyCost(collectedItem: CollectedItem): Int {
		return (collectedItem.value * config.buyMultiplier).roundToInt()
	}

	private fun openBuyMenu(player: Player, station: EcoStation) {
		val items: List<CollectedItem> = itemCache[station._id]
			.takeIf { it.isNotEmpty() } ?: error("No items available at ${station.name}")

		val freeSpace: Int = player.inventory.storageContents.count { it == null || it.type == Material.AIR }

		MenuHelper.apply {
			val buttons: List<GuiItem> = items.map { collectedItem ->
				val icon: ItemStack = createItem(collectedItem)

				val cost: String = getBuyCost(collectedItem).toCreditsString()
				val fillCost: String = getBuyCost(collectedItem).times(freeSpace).toCreditsString()
				val stock: String = if (collectedItem.stock == 0) "&c0" else "&a${collectedItem.stock}"

				icon.lore(
					listOf(
						Component.text("Cost per stack: ").color(NamedTextColor.GRAY)
							.append(Component.text(cost).color(NamedTextColor.RED)),
						Component.text("Cost to fill remaining slots: ").color(NamedTextColor.GRAY)
							.append(Component.text(fillCost).color(NamedTextColor.RED)),
						Component.text("Available Stacks: ").color(NamedTextColor.GRAY)
							.append(Component.text(stock).color(NamedTextColor.GREEN)),
						Component.text("(Left click to buy one stack)").color(NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC)),
						Component.text("(SHIFT left click to fill remaining slots)").color(NamedTextColor.GRAY).style(Style.style(TextDecoration.ITALIC))
					)
				)

				return@map guiButton(icon) {
					val playerClicker = playerClicker
					tryBuy(playerClicker, station._id, collectedItem, isShiftClick)
					playerClicker.closeInventory()
					openBuyMenu(playerClicker, station)
				}
			}

			player.openPaginatedMenu("${station.name} Exports", buttons)
		}
	}

	private fun tryCompleteMission(player: Player, stationId: Oid<EcoStation>, mission: CollectionMission) {
		require(Bukkit.isPrimaryThread()) // cache shouldn't be modified concurrently

		val station = EcoStations[stationId]

		if (!missions[stationId].contains(mission)) { // This could happen if someone else turned in the mission before they clicked the button.
			player.userError("Mission is no longer available.")
			return
		}

		val item: CollectedItem = mission.item
		val itemStack: ItemStack = createItem(item).clone()

		val fullStackSlots: List<Int> = getMatchingFullStackSlots(itemStack, player, mission.stacks)

		if (fullStackSlots.size < mission.stacks) {
			player.sendMessage(
				Component.text("You don't have enough of ").color(NamedTextColor.RED)
					.append(itemStack.displayNameComponent)
					.append(
						Component.text(
							"You need ${mission.stacks} stack(s), but only have ${fullStackSlots.size} stack(s)."
						).color(NamedTextColor.RED)
					)
			)

			return
		}

		removeFullStacks(fullStackSlots, player)

		val money: Double = giveMoney(mission, player)

		replaceWithNewMission(mission, station)

		incrementLocalStock(item, mission)
		incrementDatabaseValues(item, mission)
		giveXP(player, mission)

		player msg "&2Completed collection mission! " +
			"Delivered &a${mission.stacks}&2 stack(s) of &f${itemStack.displayNameString}&2 " +
			"and received &6${money.toCreditsString()}&2."
	}

	private fun removeFullStacks(fullStackSlots: List<Int>, player: Player) {
		fullStackSlots.forEach { player.inventory.setItem(it, null) }
	}

	private fun getMatchingFullStackSlots(itemStack: ItemStack, player: Player, stacks: Int): List<Int> {
		val customItem: CustomItem? = CustomItems[itemStack]

		// slots of the full stack items that match the collector mission's item type
		return player.inventory.contents!!
			.withIndex()
			.filter { it.value != null }
			.filter {
				when (customItem) {
					null -> it.value!!.isSimilar(itemStack) && it.value!!.amount == it.value!!.maxStackSize
					else -> customItem == CustomItems[it.value] && it.value!!.amount == customItem.material.maxStackSize
				}
			}
			// limit to the amount of stacks to avoid taking more stacks than required if they're carrying extra
			.take(stacks)
			.map { it.index }
	}

	private fun incrementDatabaseValues(item: CollectedItem, mission: CollectionMission) {
		Tasks.async {
			CollectedItem.updateById(
				item._id,
				inc(CollectedItem::stock, mission.stacks),
				inc(CollectedItem::sold, mission.stacks)
			)
		}
	}

	private fun giveMoney(mission: CollectionMission, player: Player): Double {
		val reward: Double = mission.reward.toDouble()
		VAULT_ECO.depositPlayer(player, reward)
		return reward
	}

	private fun replaceWithNewMission(mission: CollectionMission, station: EcoStation) {
		val missionList = missions[station._id]
		missionList[missionList.indexOf(mission)] = generateMission(station/*, mission.item*/)
	}

	private fun incrementLocalStock(item: CollectedItem, mission: CollectionMission) {
		item.stock += mission.stacks
	}

	private fun giveXP(player: Player, mission: CollectionMission) {
		SLXP.addAsync(player, mission.xp)
	}

	private fun tryBuy(player: Player, stationId: Oid<EcoStation>, collectedItem: CollectedItem, shiftClick: Boolean) {
		require(Bukkit.isPrimaryThread()) // cache shouldn't be concurrently modified

		if (!itemCache[stationId].contains(collectedItem)) { // idk why this would happen but it might
			return
		}

		if (collectedItem.stock <= 0) {
			return player msg "&cItem is out of stock."
		}

		val maxBuy: Int = getMaxBuy(shiftClick, player)

		if (maxBuy == 0) {
			return player msg "&cInventory is full."
		}

		val costPerStack: Double = getBuyCost(collectedItem).toDouble()
		val buyAmount = getBuyAmount(maxBuy, player, costPerStack, collectedItem)

		if (buyAmount == -1) {
			return
		} else if (buyAmount == 0) {
			player msg "&cYou can't afford that! It costs ${costPerStack.toCreditsString()} per stack, " +
				"but you only have ${VAULT_ECO.getBalance(player)}."
			return
		}

		val itemStack: ItemStack = getPurchasedItem(collectedItem, stationId, costPerStack)

		val totalCost: Double = takeMoney(costPerStack, buyAmount, player)
		giveItems(buyAmount, player, itemStack)

		updateLocalValue(collectedItem, buyAmount)
		updateDatabaseValue(collectedItem, buyAmount)

		player msg "&2Bought &a$buyAmount stack(s)&2 of &f${itemStack.displayNameString}&2 for &6${totalCost.toCreditsString()}&2. " +
			"Remaining stacks in stock: &e${collectedItem.stock}&2."
	}

	private fun getPurchasedItem(collectedItem: CollectedItem, stationId: Oid<EcoStation>, costPerStack: Double) =
		createItem(collectedItem).apply {
			amount = maxStackSize
			lore = listOf(
				"&6Purchased from &b${EcoStations[stationId].name}",
				"&6for &e${costPerStack.toCreditsString()}".colorize()
			)
		}

	private fun getMaxBuy(shiftClick: Boolean, player: Player): Int {
		return when {
			shiftClick -> player.inventory.storageContents!!.count { it == null || it.type == Material.AIR }
			else -> 1
		}
	}

	private fun getBuyAmount(maxBuy: Int, player: Player, costPerStack: Double, collectedItem: CollectedItem): Int {
		var boughtStacks = 0

		for (i: Int in 1..maxBuy) {
			if (!VAULT_ECO.has(player, (boughtStacks + 1) * costPerStack)) {
				break
			}

			if ((boughtStacks + 1) > collectedItem.stock) {
				player msg "&cOut of stock!"

				if (boughtStacks == 0) {
					return -1
				} else {
					break
				}
			}

			boughtStacks++
		}
		return boughtStacks
	}

	private fun takeMoney(costPerStack: Double, boughtStacks: Int, player: Player): Double {
		val totalCost: Double = costPerStack * boughtStacks
		VAULT_ECO.withdrawPlayer(player, totalCost)
		return totalCost
	}

	private fun giveItems(boughtStacks: Int, player: Player, itemStack: ItemStack) {
		for (i in 1..boughtStacks) {
			player.inventory.addItem(itemStack).let { remaining: HashMap<Int, ItemStack> ->
				remaining.values.forEach { remainingItem: ItemStack ->
					player.world.dropItem(player.location, remainingItem)
				}
			}
		}
	}

	private fun updateLocalValue(collectedItem: CollectedItem, boughtStacks: Int) {
		collectedItem.stock -= boughtStacks
	}

	private fun updateDatabaseValue(collectedItem: CollectedItem, boughtStacks: Int) {
		Tasks.async {
			CollectedItem.updateById(collectedItem._id, inc(CollectedItem::stock, boughtStacks))
		}
	}
}
