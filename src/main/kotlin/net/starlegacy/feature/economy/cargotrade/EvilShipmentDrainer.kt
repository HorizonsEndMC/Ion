package net.starlegacy.feature.economy.cargotrade

import com.mongodb.client.FindIterable
import java.util.Calendar
import java.util.Date
import java.util.GregorianCalendar
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.min
import net.starlegacy.PLUGIN
import net.starlegacy.database.schema.economy.CargoCrateShipment
import net.starlegacy.database.schema.misc.SLPlayer
import net.starlegacy.database.schema.misc.SLPlayerId
import net.starlegacy.database.uuid
import net.starlegacy.feature.progression.LEVEL_BALANCING
import net.starlegacy.feature.progression.Levels
import net.starlegacy.util.depositMoney
import net.starlegacy.util.getMoneyBalance
import net.starlegacy.util.toCreditsString
import net.starlegacy.util.withdrawMoney
import org.bukkit.Bukkit
import org.litote.kmongo.and
import org.litote.kmongo.gte
import org.litote.kmongo.lte

object EvilShipmentDrainer {
	class ShipmentBundle(val shipment: CargoCrateShipment) {
		private val shipments: MutableList<CargoCrateShipment> = mutableListOf(shipment)
		val averageRevenue get() = shipments.sumOf { it.crateRevenue } / shipments.size
		val lowestRevenue get() = shipments.minByOrNull { it.crateRevenue }?.crateRevenue ?: 0.0
		val totalSoldCrates get() = shipments.sumOf { min(it.totalCrates, it.soldCrates) }

		fun matchesShipment(other: CargoCrateShipment): Boolean = shipment.player == other.player
			&& shipment.crate == other.crate
			&& abs(other.claimTime.time - shipment.claimTime.time) < 1000 * 60 // one minute
			&& shipment.originTerritory == other.originTerritory
			&& shipment.destinationTerritory == other.destinationTerritory

		fun addShipment(shipment: CargoCrateShipment) {
			shipments.add(shipment)
		}
	}

	fun drain(forReal: Boolean) {
		val lists = getShipmentLists()
		for ((playerId, list) in lists) {
			drainUser(playerId, list, forReal)
		}
	}

	private val BUG_START: Date = GregorianCalendar(2020, Calendar.JANUARY, 15).time
	private val BUG_END: Date = GregorianCalendar(2020, Calendar.MAY, 8).time

	private fun getShipmentLists(): Map<SLPlayerId, List<ShipmentBundle>> {
		val map = mutableMapOf<SLPlayerId, MutableList<ShipmentBundle>>()
		for (shipment in findAffectedCrates()) {
			addShipment(shipment, map)
		}
		return map
	}

	private fun findAffectedCrates(): FindIterable<CargoCrateShipment> {
		val filter = and(CargoCrateShipment::claimTime gte BUG_START, CargoCrateShipment::claimTime lte BUG_END)
		return CargoCrateShipment.find(filter)
	}

	private fun addShipment(shipment: CargoCrateShipment, map: MutableMap<SLPlayerId, MutableList<ShipmentBundle>>) {
		val playerId = shipment.player

		if (!map.containsKey(playerId)) {
			map[shipment.player] = mutableListOf(ShipmentBundle(shipment))
			return
		}

		val bundles: MutableList<ShipmentBundle> = map.getValue(playerId)
		val bundle = bundles.firstOrNull { it.matchesShipment(shipment) }
		if (bundle == null) {
			bundles.add(ShipmentBundle(shipment))
			return
		}

		bundle.addShipment(shipment)
	}

	private fun drainUser(playerId: SLPlayerId, list: List<ShipmentBundle>, forReal: Boolean) {
		val extraMoney = getExtraMoney(list)
		if (extraMoney > 0) {
			val name = SLPlayer.getName(playerId) ?: playerId.toString()
			val totalMoney = list.sumOf { it.averageRevenue * it.totalSoldCrates }
			println("[EvilShipmentDrainer]  Taking ${extraMoney.toCreditsString()}/${totalMoney.toCreditsString()} from $name")
			drainMoney(playerId, extraMoney, forReal)
		}
	}

	private const val INVENTORY_SIZE = 9 * 4

	private fun getExtraMoney(list: List<ShipmentBundle>): Double = list.sumOf(this::getExtraMoney)

	private fun getExtraMoney(bundle: ShipmentBundle): Double =
		getExtraCrates(bundle) * bundle.lowestRevenue * .8

	private fun getExtraCrates(bundle: ShipmentBundle): Int {
		var extraCrates = 0
		var remainingCrates = bundle.totalSoldCrates
		var soldCrates = 0
		while (remainingCrates > 0) {
			extraCrates = soldCrates
			val increment = min(remainingCrates, INVENTORY_SIZE)
			soldCrates += increment
			remainingCrates -= increment
		}
		return extraCrates
	}

	private fun drainMoney(playerId: SLPlayerId, money: Double, forReal: Boolean) {
		val remaining = vaultWithdraw(playerId, money, forReal)
		if (remaining > 0) {
			drainLevels(playerId, remaining, forReal)
		}
	}

	private fun vaultWithdraw(playerId: SLPlayerId, money: Double, forReal: Boolean): Double {
		var remaining = money
		try {
			val offlinePlayer = Bukkit.getOfflinePlayer(playerId.uuid)
			val bal = offlinePlayer.getMoneyBalance()
			val available = max(0.0, bal - 50_000.00)
			val reduction = min(available, remaining)
			println("[EvilShipmentDrainer]    -> Withdraw ${reduction.toCreditsString()} from ${offlinePlayer.name}")
			if (forReal) offlinePlayer.withdrawMoney(reduction)
			remaining -= reduction
		} catch (e: Exception) {
			e.printStackTrace()
		} finally {
			return remaining
		}
	}

	private fun drainLevels(playerId: SLPlayerId, money: Double, forReal: Boolean) {
		val originalLevel = SLPlayer.getLevel(playerId)
			?: return PLUGIN.logger.warning("Failed to get level for player $playerId; could not take ${money.toCreditsString()} credits")
		var level = originalLevel
		var remainingMoney = money
		while (level >= 1) {
			val cost = LEVEL_BALANCING.creditsPerXP * Levels.getLevelUpCost(level)
			if (remainingMoney <= cost) {
				break
			}
			remainingMoney -= cost
			level--
		}
		if (originalLevel != level) {
			println("[EvilShipmentDrainer]    -> Change level of ${SLPlayer.getName(playerId)} from $originalLevel to $level")
			if (forReal) SLPlayer.setLevel(playerId, level)
		}
	}

	fun refund(percent: Double) {
		val lists = getShipmentLists()
		for ((playerId, list) in lists) {
			refundUser(playerId, list, percent)
		}
	}

	private fun refundUser(playerId: SLPlayerId, list: List<ShipmentBundle>, percent: Double) {
		val extraMoney = getExtraMoney(list)
		val refund = extraMoney * percent
		if (refund > 0) {
			val name = SLPlayer.getName(playerId) ?: playerId.toString()
			if (vaultDeposit(playerId, refund))
				println("Refunded $name ${refund.toCreditsString()}")
			else
				println("Failed to refund $name")
		}
	}

	private fun vaultDeposit(playerId: SLPlayerId, amount: Double): Boolean {
		try {
			val offlinePlayer = Bukkit.getOfflinePlayer(playerId.uuid)
			offlinePlayer.depositMoney(amount)
			return true
		} catch (e: Exception) {
			e.printStackTrace()
			return false
		}
	}
}
