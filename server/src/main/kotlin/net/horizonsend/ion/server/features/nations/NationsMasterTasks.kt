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
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.utils.ACTIVE_AFTER_TIME
import net.horizonsend.ion.server.features.nations.utils.INACTIVE_BEFORE_TIME
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.VAULT_ECO
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.kyori.adventure.text.minimessage.MiniMessage
import org.bukkit.Bukkit
import org.litote.kmongo.and
import org.litote.kmongo.contains
import org.litote.kmongo.eq
import org.litote.kmongo.gte
import org.litote.kmongo.ne
import java.lang.Integer.min

object NationsMasterTasks : IonServerComponent() {
	override fun onEnable() {
		if (ConfigurationFiles.legacySettings().master) {
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

		val results: ProjectedResults = Settlement.findPropsById(settlementId, Settlement::name, Settlement::territory) ?: return

		val name: String = results[Settlement::name]
		val territoryId: Oid<Territory> = results[Settlement::territory]
		val territory: RegionTerritory = Regions[territoryId]

		ServerInboxes.sendServerMessages(
			recipients = Settlement.getMembers(settlementId),
			subject = Component.text("Settlement Purged.", NamedTextColor.RED),
			content = template(Component.text("Your settlement, {0} was purged due to 30 days of complete inactivity.", NamedTextColor.RED), name)
		)

		Settlement.delete(settlementId)

		if (sendMessage) {
			val message = "<red>Settlement $name on ${territory.world} at ${territory.centerX}, ${territory.centerZ} " +
				"was purged for ${NATIONS_BALANCE.settlement.inactivityDays}+ days of complete inactivity."
			Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
		}
	}

	fun purgeNation(nationId: Oid<Nation>, sendMessage: Boolean) {
		val nation = Nation.findById(nationId) ?: return

		ServerInboxes.sendServerMessages(
			recipients = Nation.getMembers(nationId),
			subject = Component.text("Nation Purged.", NamedTextColor.RED),
			content = template(Component.text("Your nation, {0} was purged due to the capital settlement being inactive for 30 days.", NamedTextColor.RED), nation.name)
		)

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
			
			val activeMembers: Long = SLPlayer.count(
					and(SLPlayer::settlement eq settlementId, SLPlayer::lastSeen gte ACTIVE_AFTER_TIME)
			)

			val hasMoney: Boolean = money >= tax
			val hasMembers: Boolean = activeMembers >= NATIONS_BALANCE.settlement.cityMinActive

			val name = settlementResults[Settlement::name]
			val taxCredits = tax.toCreditsString()

			if (isActive) {
			//currently protected
				if (!hasMoney) {
					//members but no money
					Settlement.setCityState(settlementId, Settlement.CityState.UNPAID)
					val message = "<red>Player Trade City $name failed to pay its hourly tax of $taxCredits! Until it pays its tax, it does not have settlement city protection."
					Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
				} else if (!hasMembers) {
					//money but no members
					Settlement.setCityState(settlementId, Settlement.CityState.UNPAID)
					val message = "<red>Player Trade City $name paid its tax but didn't have enough active members! It needs at least ${NATIONS_BALANCE.settlement.cityMinActive} for protection."
					Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
				}
			} else {
			//currently unprotected
				if (hasMoney && hasMembers) {
					//has both money and members
					Settlement.setCityState(settlementId, Settlement.CityState.ACTIVE)
					val message = "<dark_green>Player Trade City $name has paid its hourly tax of $taxCredits and has enough active members, so it's protected!"
					Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(message))
				}
			}
		}
	}

	private fun doZoneRent() {
		for (zone in Regions.getAllOf<RegionSettlementZone>()) {
			val owner: SLPlayerId = zone.owner ?: continue
			val rent: Int = zone.cachedRent ?: continue

			val offlinePlayer = Bukkit.getOfflinePlayer(owner.uuid)

			if (!VAULT_ECO.has(offlinePlayer, rent.toDouble())) {
				Notify.settlementCrossServer(zone.settlement, MiniMessage.miniMessage().deserialize("<red>${offlinePlayer.name} failed to pay rent for zone ${zone.name}"))

				ServerInboxes.settlementMessage(zone.settlement, template(Component.text("{0} failed to pay rent for zone {1}", RED), offlinePlayer.name, zone.name))
					.setSubject(Component.text("Failure to Pay Zone Rent", RED))
					.setAllowDuplicates(false)
					.filterRecipients { playerId ->
						if (Settlement.matches(zone.settlement, Settlement::leader eq playerId)) {
							return@filterRecipients true// leaders have all perms
						}

						SettlementRole.any(and(
							SettlementRole::parent eq zone.settlement, // just in case, but should never have a role from another settlement
							SettlementRole::members contains playerId,
							SettlementRole::permissions contains SettlementRole.Permission.MANAGE_ZONES
						))
					}
					.send()

				ServerInboxes.settlementMessage(zone.settlement, template(Component.text("You failed to pay {0} in rent for zone {1}", RED), rent.toCreditComponent(), zone.name))
					.setSubject(Component.text("Failure to Pay Zone Rent", RED))
					.setAllowDuplicates(false)
					.send()

				continue
			}

			VAULT_ECO.withdrawPlayer(offlinePlayer, rent.toDouble())
			Settlement.deposit(zone.settlement, rent)

			Notify.playerCrossServer(owner.uuid, MiniMessage.miniMessage().deserialize("Paid ${rent.toCreditsString()} rent for zone ${zone.id}"))
		}
	}
}
