package net.horizonsend.ion.server.features.nations

import com.mongodb.client.MongoIterable
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.ProjectedResults
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.LegacySettings
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.utils.ACTIVE_AFTER_TIME
import net.horizonsend.ion.server.features.nations.utils.INACTIVE_BEFORE_TIME
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.litote.kmongo.and
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.ne
import java.lang.Integer.min

object NationsMasterTasks : IonServerComponent() {
	override fun onEnable() {
		if (LegacySettings.master) {
			// 20 ticks * 60 = 1 minute, 20 ticks * 60 * 60 = 1 hour
			Tasks.asyncRepeat(20 * 60, 20 * 60 * 60, ::executeAll)
		}
	}

	private fun executeAll() {
		checkPurges()

		executeMoneyTasks()
	}

	fun checkPurges() {
		for (id: Oid<Settlement> in Settlement.allIds()) {
			val query = and(SLPlayer::settlement eq id, SLPlayer::lastSeen gte INACTIVE_BEFORE_TIME)

			if (SLPlayer.none(query)) {
				purgeSettlement(id, true)
			}
		}
	}

	fun purgeSettlement(settlementId: Oid<Settlement>, sendMessage: Boolean) {
		if (Settlement.isCapital(settlementId)) {
			val nationId = Settlement.findPropById(settlementId, Settlement::nation)

			if (nationId != null) {
				purgeNation(nationId, sendMessage)
			}
		}

		val results: ProjectedResults = Settlement.findPropsById(settlementId, Settlement::name, Settlement::territory)
			?: return

		val name: String = results[Settlement::name]
		val territoryId: Oid<Territory> = results[Settlement::territory]
		val territory: RegionTerritory = Regions[territoryId]

		Settlement.delete(settlementId)

		if (sendMessage) {
			val message = "<red>Settlement $name on ${territory.world} at ${territory.centerX}, ${territory.centerZ} " +
				"was purged for ${NATIONS_BALANCE.settlement.inactivityDays}+ days of complete inactivity."
			Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
		}
	}

	fun purgeNation(nationId: Oid<Nation>, sendMessage: Boolean) {
		val nation = Nation.findById(nationId) ?: return

		Nation.delete(nationId)

		if (sendMessage) {
			val message = "<red>Nation ${nation.name} had its capital settlement purge and was purged itself!"
			Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
		}
	}

	fun executeMoneyTasks() {
		doActivityCredits()

		doCityCheck()

		doZoneRent()
	}

	private fun doActivityCredits() {
		for (nationId: Oid<Nation> in Nation.allIds()) {
			val nation: NationCache.NationData = NationCache[nationId]

			// Give the nation its station income if it has stations
			val stationCount = min(CapturableStation.count(CapturableStation::nation eq nationId).toInt(), 4)
			val stationIncome = if (stationCount > 2) stationCount * 75 else stationCount * 50

			if (stationIncome > 0) {
				Nation.deposit(nationId, stationIncome)
				Notify.nationCrossServer(
					nationId,
					MiniMessage.miniMessage().deserialize(
						"<gold>Your nation received <yellow>${stationIncome.toCreditsString()}<gold> credits " +
							"from captured space station hourly income with <dark_aqua>$stationCount<gold> stations"
					)
				)
			}

			val activeCount = SLPlayer.count(
				and(SLPlayer::lastSeen gte ACTIVE_AFTER_TIME, SLPlayer::nation eq nationId)
			).toInt()
			val activityCredits = activeCount * NATIONS_BALANCE.nation.hourlyActivityCredits

			if (activityCredits > 0) {
				Nation.deposit(nationId, activityCredits)
				Notify.playerCrossServer(
					nation.leader.uuid,
					MiniMessage.miniMessage().deserialize(
						"<dark_green>Your nation received <gold>${activityCredits.toCreditsString()}<dark_green> " +
							"for activity credits from <yellow>$activeCount<dark_green> active members"
					)
				)
			}
		}

		for (settlementId: Oid<Settlement> in Settlement.allIds()) {
			val settlement: SettlementCache.SettlementData = SettlementCache[settlementId]

			val activeCount = SLPlayer.count(
				and(SLPlayer::lastSeen gte ACTIVE_AFTER_TIME, SLPlayer::settlement eq settlementId)
			).toInt()
			val activityCredits = activeCount * NATIONS_BALANCE.settlement.hourlyActivityCredits

			if (activityCredits > 0) {
				Settlement.deposit(settlementId, activityCredits)
				Notify.playerCrossServer(
					settlement.leader.uuid,
					MiniMessage.miniMessage().deserialize(
						"<dark_aqua>Your settlement received <gold>${activityCredits.toCreditsString()}<dark_aqua> " +
								"for activity credits from <yellow>$activeCount<dark_aqua> active members"
					)
				)
			}
		}
	}

	private fun doCityCheck() {
		val iterable: MongoIterable<ProjectedResults> = Settlement.findProps(
			Settlement::cityState ne null, Settlement::_id, Settlement::cityState, Settlement::balance,
			Settlement::name, Settlement::territory
		)

		for (settlementResults: ProjectedResults in iterable) {
			val settlementId: Oid<Settlement> = settlementResults[Settlement::_id]
			val cityState: Settlement.CityState = settlementResults[Settlement::cityState] ?: continue

			val isActive = cityState == Settlement.CityState.ACTIVE

			val money = settlementResults[Settlement::balance]
			val tax = NATIONS_BALANCE.settlement.cityHourlyTax
			var willBeActive: Boolean = money >= tax

			val name = settlementResults[Settlement::name]
			val taxCredits = tax.toCreditsString()

			if (willBeActive) {
				Settlement.withdraw(settlementId, tax)

				if (!isActive) {
					Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize("<dark_green>Player Trade City $name has paid its hourly tax of $taxCredits, so it's protected!"))
				}
			} else {
				val message = "<red>Player Trade City $name failed to pay its hourly tax of $taxCredits! " +
					"Until it pays its tax, it does not have settlement city protection."
				Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize(message))
			}

			if (willBeActive) {
				val activeMembers: Long = SLPlayer.count(
					and(SLPlayer::settlement eq settlementId, SLPlayer::lastSeen gte ACTIVE_AFTER_TIME)
				)

				if (activeMembers < NATIONS_BALANCE.settlement.cityMinActive) {
					Notify.chatAndGlobal(MiniMessage.miniMessage().deserialize("<red>Player Trade City $name paid its tax but didn't have enough active members! It needs at least ${NATIONS_BALANCE.settlement.cityMinActive} for protection."))
					willBeActive = false
				}
			}

			if (isActive == willBeActive) {
				continue
			}

			val newState = if (willBeActive) Settlement.CityState.ACTIVE else Settlement.CityState.UNPAID
			Settlement.setCityState(settlementId, newState)
		}
	}

	private fun doZoneRent() {
		for (zone in Regions.getAllOf<RegionSettlementZone>()) {
			val owner: SLPlayerId = zone.owner ?: continue
			val rent: Int = zone.cachedRent ?: continue

			val offlinePlayer = Bukkit.getOfflinePlayer(owner.uuid)

			if (!VAULT_ECO.has(offlinePlayer, rent.toDouble())) {
				Notify.settlementCrossServer(zone.settlement, MiniMessage.miniMessage().deserialize("<red>${offlinePlayer.name} failed to pay rent for zone ${zone.name}"))
				continue
			}

			VAULT_ECO.withdrawPlayer(offlinePlayer, rent.toDouble())
			Settlement.deposit(zone.settlement, rent)

			Notify.playerCrossServer(owner.uuid, MiniMessage.miniMessage().deserialize(
				"Paid ${rent.toCreditsString()} rent for zone ${zone.id}"
				)
			)
		}
	}
}
