package net.horizonsend.ion.server.features.nations

import com.mongodb.client.MongoIterable
import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.ProjectedResults
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.SettlementCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.database.schema.misc.SLPlayerId
import net.horizonsend.ion.common.database.schema.nations.CapturableStation
import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveSiegeData
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRole
import net.horizonsend.ion.common.database.schema.nations.RegionalObjectiveType
import net.horizonsend.ion.common.database.schema.nations.Settlement
import net.horizonsend.ion.common.database.schema.nations.SettlementRole
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.database.schema.nations.Territory
import net.horizonsend.ion.common.database.uuid
import net.horizonsend.ion.common.utils.miscellaneous.toCreditsString
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.common.utils.text.toCreditComponent
import net.horizonsend.ion.server.command.GlobalCompletions
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.misc.ServerInboxes
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.nations.region.types.RegionRegionalObjective
import net.horizonsend.ion.server.features.nations.region.types.RegionSettlementZone
import net.horizonsend.ion.server.features.nations.region.types.RegionStationZone
import net.horizonsend.ion.server.features.nations.region.types.RegionTerritory
import net.horizonsend.ion.server.features.nations.utils.ACTIVE_AFTER_TIME
import net.horizonsend.ion.server.features.nations.utils.INACTIVE_BEFORE_TIME
import net.horizonsend.ion.server.features.player.Power
import net.horizonsend.ion.server.features.space.spacestations.CachedNationSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedPlayerSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.CachedSettlementSpaceStation
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
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
import java.time.ZonedDateTime

object NationsMasterTasks : IonServerComponent() {
	override fun onEnable() {
		Tasks.async {
			for (nation in Nation.all()) {
				if (nation.siegeable == null) nation.siegeable = false
			}
		}

		if (ConfigurationFiles.legacySettings().master) {
			// 20 ticks * 60 = 1 minute, 20 ticks * 60 * 60 = 1 hour
			Tasks.asyncRepeat(20 * 60, 20 * 60 * 60, ::executeAll)
			Tasks.asyncAtHour(0, ::doTerritoryUpkeep)
		}
	}

	data class TerritoryEntry(
		val name: String,
		val isDominion: Boolean,
		val dominionId: Oid<DominionTerritory>? = null,
		val normalId: Oid<Territory>? = null
	)

	const val UPKEEP_COST = 2000

	private fun executeAll() {
		checkPurges()
		executeMoneyTasks()
		recalculateNationPower()
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
			/*
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
			 */

			/*
			val solarSiegeCount = SolarSiegeZone.count(SolarSiegeZone::nation eq nationId).toInt()
			val solarSiegeIncome = solarSiegeCount * 100

			if (solarSiegeIncome > 0) {
				Nation.deposit(nationId, solarSiegeIncome)
				Notify.nationCrossServer(
					nationId,
					template(Component.text("Your nation received {0} credits of hourly income for owning {1} solar siege zones.", HE_MEDIUM_GRAY), solarSiegeIncome.toCreditComponent(), stationCount)
				)
			}
			 */

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

		// Regional objective passive generation
		val xenon = CustomItemKeys.GAS_CANISTER_XENON.getValue()
		val canister = xenon.createWithFill(xenon.maximumFill)
		val itemString = GlobalCompletions.toItemString(canister)

		for (nationId in Nation.allIds()) {
			// Gas depot passive xenon
			val ownedDepots = Regions.getAllOf<RegionRegionalObjective>()
				.filter { it.nation == nationId && it.type == RegionalObjectiveType.GAS_DEPOT }
			for (depot in ownedDepots) {
				val rewardMap = mutableMapOf(itemString to 4)
				RegionalObjectiveSiegeData.create(depot.id, nationId, rewardMap, passive = true)
				Notify.nationCrossServer(nationId, MiniMessage.miniMessage().deserialize(
					"<gold>Your nation received 4 Xenon Canisters from Gas Depot ${depot.name}!"
				))
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
					.setSubject(Component.text("Renter Failed to Pay Zone Rent", RED))
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

				ServerInboxes.sendMessage(zone.owner!!,
					Component.text("Settlement of ${SettlementCache[zone.settlement].name}"),
					Component.text("Failure to Pay Zone Rent", RED),
					template(Component.text("You failed to pay {0} in rent for zone {1}", RED), rent.toCreditComponent(), zone.name))

				continue
			}

			VAULT_ECO.withdrawPlayer(offlinePlayer, rent.toDouble())
			Settlement.deposit(zone.settlement, rent)

			Notify.playerCrossServer(owner.uuid, MiniMessage.miniMessage().deserialize("Paid ${rent.toCreditsString()} rent for zone ${zone.id}"))
		}

		for (zone in Regions.getAllOf<RegionStationZone>()) {
			val station = SpaceStationCache[zone.station] ?: continue
			val owner: SLPlayerId = zone.owner ?: continue
			val rent: Int = zone.cachedRent ?: continue

			val offlinePlayer = Bukkit.getOfflinePlayer(owner.uuid)

			if (!VAULT_ECO.has(offlinePlayer, rent.toDouble())) {
				when (station) {
					is CachedPlayerSpaceStation -> Notify.playerCrossServer(station.owner.uuid, MiniMessage.miniMessage().deserialize("<red>${offlinePlayer.name} failed to pay rent for zone ${zone.name}"))
					is CachedSettlementSpaceStation -> Notify.settlementCrossServer(station.owner, MiniMessage.miniMessage().deserialize("<red>${offlinePlayer.name} failed to pay rent for zone ${zone.name}"))
					is CachedNationSpaceStation -> Notify.nationCrossServer(station.owner, MiniMessage.miniMessage().deserialize("<red>${offlinePlayer.name} failed to pay rent for zone ${zone.name}"))
					else -> continue
				}

				when (station) {
					is CachedPlayerSpaceStation -> {
						ServerInboxes.sendMessage(
							station.owner,
							Component.text("Station ${SpaceStationCache[zone.station]?.name ?: continue}"),
							Component.text("Renter Failed to Pay Zone Rent"),
							template(Component.text("{0} failed to pay rent for zone {1}", RED), offlinePlayer.name, zone.name)
						)
					}
					is CachedSettlementSpaceStation -> {
						ServerInboxes.settlementMessage(station.owner, template(Component.text("{0} failed to pay rent for zone {1}", RED), offlinePlayer.name, zone.name))
							.setSubject(Component.text("Renter Failed to Pay Zone Rent", RED))
							.setAllowDuplicates(false)
							.filterRecipients { playerId ->
								if (Settlement.matches(station.owner, Settlement::leader eq playerId)) {
									return@filterRecipients true// leaders have all perms
								}

								SettlementRole.any(and(
									SettlementRole::parent eq station.owner, // just in case, but should never have a role from another settlement
									SettlementRole::members contains playerId,
									SettlementRole::permissions contains SettlementRole.Permission.MANAGE_ZONES
								))
							}
							.send()
					}
					is CachedNationSpaceStation -> {
						ServerInboxes.nationMessage(station.owner, template(Component.text("{0} failed to pay rent for zone {1}", RED), offlinePlayer.name, zone.name))
							.setSubject(Component.text("Renter Failed to Pay Zone Rent", RED))
							.setAllowDuplicates(false)
							.filterRecipients { playerId ->
								if (Nation.matches(station.owner, Settlement::leader eq playerId)) {
									return@filterRecipients true// leaders have all perms
								}

								SettlementRole.any(and(
									NationRole::parent eq station.owner, // just in case, but should never have a role from another settlement
									NationRole::members contains playerId,
									NationRole::permissions contains NationRole.Permission.MANAGE_STATION
								))
							}
							.send()
					}
					else -> continue
				}

				ServerInboxes.sendMessage(zone.owner!!,
					Component.text("Station of ${SpaceStationCache[zone.station]?.name ?: continue}"),
					Component.text("Failure to Pay Zone Rent", RED),
					template(Component.text("You failed to pay {0} in rent for zone {1}", RED), rent.toCreditComponent(), zone.name))

				continue
			}

			VAULT_ECO.withdrawPlayer(offlinePlayer, rent.toDouble())

			when (station) {
				is CachedPlayerSpaceStation -> VAULT_ECO.depositPlayer(Bukkit.getOfflinePlayer(station.owner.uuid), rent.toDouble())
				is CachedSettlementSpaceStation -> Settlement.deposit(station.owner, rent)
				is CachedNationSpaceStation -> Nation.deposit(station.owner, rent)
			}

			Notify.playerCrossServer(owner.uuid, MiniMessage.miniMessage().deserialize("Paid ${rent.toCreditsString()} rent for zone ${zone.id}"))
		}
	}

	private fun doTerritoryUpkeep() {
		for (nationId in Nation.allIds()) {
			val dominionTerritories = Regions.getAllOf<RegionDominionTerritory>()
				.filter { it.nation == nationId }

			val normalTerritories = Territory.find(
				and(Territory::nation eq nationId, Territory::settlement eq null)
			).toList()

			val pool = dominionTerritories.map { TerritoryEntry(it.name, true, it.id) } +
				normalTerritories.map { TerritoryEntry(it.name, false, normalId = it._id) }

			val totalCount = pool.size
			if (totalCount == 0) continue

			val totalCost = totalCount * UPKEEP_COST
			val balance = Nation.findPropById(nationId, Nation::balance) ?: continue
			val nationName = NationCache[nationId].name

			if (balance >= totalCost) {
				Nation.withdraw(nationId, totalCost)
				Notify.nationCrossServer(nationId, MiniMessage.miniMessage().deserialize(
					"<gold>Your nation paid <yellow>${totalCost.toCreditsString()}<gold> in territory upkeep for <dark_aqua>$totalCount<gold> territories."
				))
			} else {
				Nation.withdraw(nationId, balance)

				val chosen = pool.random()
				val name = chosen.name

				if (chosen.isDominion) {
					DominionTerritory.setNation(chosen.dominionId!!, null)
				} else {
					Territory.setNation(chosen.normalId!!, null)
				}

				Notify.chatAndEvents(MiniMessage.miniMessage().deserialize(
					"<red>$nationName's territory $name has been unclaimed due to an inability to pay upkeep!"
				))

				ServerInboxes.sendServerMessages(
					recipients = Nation.getMembers(nationId),
					subject = Component.text("Nation Territory Unclaimed.", NamedTextColor.RED),
					content = template(Component.text("Your nation's territory, {0}, was purged as your nation could not afford the territory upkeep cost of <yellow>${totalCost.toCreditsString()}.", NamedTextColor.RED), name)

				)
			}
		}
	}


	fun recalculateNationPower() {
		for (id: Oid<Nation> in Nation.allIds()) {
			Power.recalculateNationPower(id)
		}
	}
}
