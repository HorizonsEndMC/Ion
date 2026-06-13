package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.DominionTerritory
import net.horizonsend.ion.common.database.schema.nations.DominionTerritorySiegeData
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionDominionTerritory
import net.horizonsend.ion.server.features.nations.utils.ACTIVE_AFTER_TIME
import net.horizonsend.ion.server.features.player.Power.dominionTerritoryCost
import net.horizonsend.ion.server.features.starship.TypeCategory
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object DominionTerritorySieges : IonServerComponent(true) {
	val config get() = ConfigurationFiles.nationConfiguration().dominionTerritorySiegeConfiguration

	private val recentlySiegedTerritories = mutableMapOf<Oid<DominionTerritory>, Long>() // territory id -> siege end time

	fun isOnCooldown(territoryId: Oid<DominionTerritory>): Boolean {
		val lastSieged = recentlySiegedTerritories[territoryId] ?: return false
		return System.currentTimeMillis() - lastSieged < TimeUnit.HOURS.toMillis(1)
	}

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	override fun onEnable() {
		tryLoadSieges()
		Tasks.asyncRepeat(60L, 60L) { getAllActiveSieges().filter { it.needsSave }.forEach(DominionTerritorySiege::saveSiegeData) }
		Tasks.asyncRepeat(20L * 60L, 20L * 60L, ::processPassivePoints)

		Nation.watchDeletes { handleNationDisband(it.oid) }
	}

	private val preparationSieges = mutableMapOf<Oid<DominionTerritorySiegeData>, DominionTerritorySiege>()
	private val activeSieges = mutableMapOf<Oid<DominionTerritorySiegeData>, DominionTerritorySiege>()
	private const val MIN_SHIP_SIZE = 4000

	private fun getAllPreparingSieges(): Collection<DominionTerritorySiege> = preparationSieges.values
	private fun getAllActiveSieges(): Collection<DominionTerritorySiege> = activeSieges.values

	fun getAllSieges(): Collection<DominionTerritorySiege> = getAllPreparingSieges().plus(getAllActiveSieges())

	fun setPreparing(siege: DominionTerritorySiege) {
		preparationSieges[siege.databaseId] = siege
	}

	fun setActive(siege: DominionTerritorySiege) {
		preparationSieges.remove(siege.databaseId)
		activeSieges[siege.databaseId] = siege
	}

	fun removeActive(siege: DominionTerritorySiege) {
		preparationSieges.remove(siege.databaseId) // Just in case
		activeSieges.remove(siege.databaseId)
	}

	/**
	 * Returns whether this siege zone is being prepared for a siege, or actively under siege
	 **/
	private fun isUnderSiege(stationId: Oid<DominionTerritory>): Boolean {
		return activeSieges.any { it.value.region.id == stationId } || preparationSieges.any { it.value.region.id == stationId }
	}

	/**
	 * Helper to find how deep in power debt a nation is
	 **/
	private fun maxSiegeableFromDeficit(deficit: Int): Int {
		var n = 0
		while (10 * ((n + 1) * (n + 2) / 2) <= deficit) {
			n++
		}
		return maxOf(1, n + 1) // always at least 1 if siegable
	}

	/**
	 * Returns whether the current territory belongs to a nation that has below threshold power
	 **/
	private fun canBeSieged(region: RegionDominionTerritory): Boolean {
		val nation = region.nation ?: return false
		val nationData = NationCache[nation]
		if (nationData.siegeable == false || nationData.siegeable == null) return false

		val activeSiegeCount = getAllSieges().count { it.defender == nation }

		val power = Nation.getTotalPower(nation, ACTIVE_AFTER_TIME)
		val threshold = dominionTerritoryCost(nationData)
		val deficit = threshold - power

		val maxSiegeable = maxSiegeableFromDeficit(deficit)

		return activeSiegeCount < maxSiegeable
	}

	// Initiation, Ending
	fun initSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a zone.")

		val zone = Regions.findFirstOf<RegionDominionTerritory>(player.location)
			?: return@asyncLocked player.userError("You must be within a zone's area to siege it.")

		val stationId = zone.id

		if (isUnderSiege(stationId)) {
			return@asyncLocked player.userError("This territory is already under siege!")
		}

		if (isOnCooldown(stationId)) {
			return@asyncLocked player.userError("This territory was recently sieged and cannot be sieged again for 1 hour!")
		}

		if (!isInBigShip(player)) {
			return@asyncLocked player.userError("You cannot siege in a ship smaller than $MIN_SHIP_SIZE blocks.")
		}

		if (zone.nation == nation) {
			return@asyncLocked player.userError("Your nation already owns this territory.")
		}

		if (zone.nation == null) {
			return@asyncLocked player.userError("This territory cannot be besieged!")
		}

		if (!canBeSieged(zone)) {
			player.userError("This territory is not siegable!")
			player.information("You can only siege a nation that has too low power!")
			return@asyncLocked
		}

		initSiege(zone, player.name, nation)
	}

	/**
	 * Initiates a new siege, assumes all checks have been done properly
	 **/
	private fun initSiege(region: RegionDominionTerritory, attackerName: String, attackerNation: Oid<Nation>): Boolean {
		val defender = checkNotNull(region.nation)
		val siegeData = DominionTerritorySiegeData.new(region.id, attackerNation, defender)

		val siege = DominionTerritorySiege(
			siegeData,
			region,
			attacker = attackerNation,
			defender = defender,
			declaredTime = System.currentTimeMillis()
		)

		Notify.chatAndGlobal(template(
			text("{0} of {1} has initiated a siege on {2}'s territory in {3}. The preparation period begins now.", HE_MEDIUM_GRAY),
			attackerName,
			formatNationName(attackerNation),
			formatNationName(defender),
			region.name
		))

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, Embed(
			title = "Siege Declaration",
			description = "$attackerName of ${formatNationName(attackerNation).plainText()} has declared a siege of ${formatNationName(defender).plainText()}'s territory in " +
				"${region.world}. The siege will start <t:${TimeUnit.MILLISECONDS.toSeconds(siege.getActivePeriodStart())}:R>."
		))

		siege.scheduleTasks()

		return true
	}

	fun attackerAbandonSiege(player: Player, siege: DominionTerritorySiege) {
		val playerNation = PlayerCache[player].nationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = NationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}", HE_MEDIUM_GRAY),
			useQuotesAroundObjects = false,
			player.name,
			nationName,
			siege.formatName(),
		))

		siege.fail()
	}

	fun defenderAbandonSiege(player: Player, siege: DominionTerritorySiege) {
		val playerNation = PlayerCache[player].nationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = NationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}", HE_MEDIUM_GRAY),
			useQuotesAroundObjects = false,
			player.name,
			nationName,
			siege.formatName(),
		))

		unclaim(siege.region.id)
	}

	// Point accrual
	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return

		// Find a siege in the same world as the sunk ship
		val sunkWorld = event.starship.world.name
		val siege = getAllActiveSieges().firstOrNull { it.region.world == sunkWorld } ?: return

		// Check if the ship was in the siege zone
		if (!siege.region.contains(
				event.starship.centerOfMass.x,
				event.starship.centerOfMass.y,
				event.starship.centerOfMass.z
			)) return

		val victimNation = PlayerCache[controller.player].nationOid ?: return

		var totalPoints = when {
			event.starship.type.typeCategory == TypeCategory.TRADE_SHIP -> config.miningShipKillPoints
			event.starship.initialBlockCount < 4501 -> config.subCapitalKillPoints
			event.starship.initialBlockCount in 4501..12501 -> config.capitalKillPoints
			event.starship.initialBlockCount in 12501..36000 -> config.superCapitalKillPoints
			else -> 1.0
		}
		if (event.starship.type.tech2) totalPoints *= config.tech2Multiplier

		val victimIsDefenderSide = victimNation == siege.defender ||
			(victimNation != siege.attacker && RelationCache[siege.defender, victimNation].ordinal >= NationRelation.Level.ALLY.ordinal)

		val damagers = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.filterNot { PlayerCache[(it.key as PlayerDamager).player]?.nationOid == victimNation }

		if (damagers.isEmpty()) return

		val damagePointsSum = damagers.values.sumOf { it.points.get() }

		for ((damager, damageData) in damagers) {
			val damagerPlayer = (damager as? PlayerDamager)?.player ?: continue
			val damagerNation = PlayerCache[damagerPlayer].nationOid ?: continue
			val percent = damageData.points.get().toDouble() / damagePointsSum.toDouble()
			val points = (totalPoints * percent).roundToInt()

			val damagerIsDefenderSide = damagerNation == siege.defender ||
				(damagerNation != siege.attacker && RelationCache[siege.defender, damagerNation].ordinal >= NationRelation.Level.ALLY.ordinal)

			if (victimIsDefenderSide && !damagerIsDefenderSide) {
				siege.attackerPoints += points
				log.info("Awarded attacker $points points for sinking ship of ${controller.player.name}")
			} else if (!victimIsDefenderSide && damagerIsDefenderSide) {
				siege.defenderPoints += points
				log.info("Awarded defender $points points for sinking ship of ${controller.player.name}")
			}
		}

		IonServer.server.sendMessage(template(
			text("A ship belonging to {0} was sunk during the siege of {1}!"),
			formatNationName(victimNation),
			siege.region.name
		))
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val killer = event.player.killer ?: return
		processKill(event.player, killer, config.playerKillPoints)
	}

	private fun processKill(player: Player, killer: Player, points: Int) {
		// Only process kills in the same world as the siege
		val siege = getAllActiveSieges().firstOrNull {
			it.region.world == player.world.name && it.region.contains(player.location)
		} ?: getAllActiveSieges().firstOrNull {
			it.region.world == player.world.name && it.region.contains(killer.location)
		} ?: return

		if (!killer.isOnline) return

		val playerNation = PlayerCache[player].nationOid ?: return
		val killerNation = PlayerCache[killer].nationOid ?: return

		val playerIsDefenderSide = siege.isDefender(player.slPlayerId) ||
			(playerNation != siege.attacker && RelationCache[siege.defender, playerNation].ordinal >= NationRelation.Level.ALLY.ordinal)

		val killerIsDefenderSide = siege.isDefender(killer.slPlayerId) ||
			(killerNation != siege.attacker && RelationCache[siege.defender, killerNation].ordinal >= NationRelation.Level.ALLY.ordinal)

		// Killer is attacker side, victim is defender side
		if (!killerIsDefenderSide && playerIsDefenderSide) {
			siege.attackerPoints += points
			log.info("Awarded attacker $points points for killing ${player.name}")
			IonServer.server.sendMessage(template(
				text("{0} accrued {1} points for killing {2}."),
				formatNationName(siege.attacker),
				points,
				player.name
			))
		}

		// Killer is defender side, victim is attacker side
		if (killerIsDefenderSide && !playerIsDefenderSide) {
			siege.defenderPoints += points
			log.info("Awarded defender $points points for killing ${player.name}")
			IonServer.server.sendMessage(template(
				text("{0} accrued {1} points for killing {2}."),
				formatNationName(siege.defender),
				points,
				player.name
			))
		}
	}

	private fun processPassivePoints() {
		getAllActiveSieges().forEach(::processPassivePoints)
	}

	private fun processPassivePoints(siege: DominionTerritorySiege) {
		val world = siege.region.bukkitWorld ?: return
		val starships = ActiveStarships.getInWorld(world)
		val contained = starships
			.filter { siege.region.contains(it.centerOfMass.x, it.centerOfMass.y, it.centerOfMass.z) && it.initialBlockCount >= config.minimumPassivePointsShipSize }
			.mapNotNull { it.controller as? PlayerController }

		val siegeAudience = ForwardingAudience {
			Bukkit.getOnlinePlayers().filter { player ->
				siege.region.contains(player.location)
			}
		}

		val defenderCount = contained.count { controller ->
			val nation = PlayerCache[controller.player].nationOid ?: return@count false
			siege.isDefender(controller.player.slPlayerId) ||
				(nation != siege.attacker && RelationCache[siege.defender, nation].ordinal >= NationRelation.Level.ALLY.ordinal)
		}

		val attackerCount = contained.count { controller ->
			val nation = PlayerCache[controller.player].nationOid ?: return@count false
			!siege.isDefender(controller.player.slPlayerId) &&
				(nation == siege.attacker || RelationCache[siege.defender, nation].ordinal < NationRelation.Level.ALLY.ordinal)
		}

		val defenderNew = (defenderCount.coerceAtMost(5) * config.passivePoints).roundToInt()
		if (defenderNew > 0) {
			siege.defenderPoints += defenderNew
			log.info("Awarded defender $defenderNew passive points")
			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the nation territory."),
				formatNationName(siege.defender),
				defenderNew
			))
		}

		val attackerNew = (attackerCount.coerceAtMost(5) * config.passivePoints).roundToInt()
		if (attackerNew > 0) {
			siege.attackerPoints += attackerNew
			log.info("Awarded attacker $attackerNew passive points")
			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the nation territory."),
				formatNationName(siege.attacker),
				attackerNew
			))
		}
	}

	// Load from restart
	private fun tryLoadSieges() = DominionTerritorySiegeData.findActive().map(DominionTerritorySiegeData::_id).forEach(::loadPriorSiege)

	private fun loadPriorSiege(id: Oid<DominionTerritorySiegeData>) = asyncLocked {
		val data = DominionTerritorySiegeData.findById(id) ?: return@asyncLocked

		val region = Regions.get<RegionDominionTerritory>(data.zone)
		val siege = DominionTerritorySiege(
			id,
			region,
			data.attacker,
			data.attackerPoints,
			data.defender,
			data.defenderPoints,
			DominionTerritorySiegeData.findPropById(id, DominionTerritorySiegeData::declareTime)!!.time
		)

		log.info("Resuming nation territory siege in ${region.world}")

		Notify.chatAndGlobal(ofChildren(text("Resumed ", HE_MEDIUM_GRAY), siege.formatName()))
		siege.scheduleTasks()

		if (siege.isPreparationPeriod()) {
			preparationSieges[id] = siege
		}

		if (siege.isActivePeriod()) {
			activeSieges[id] = siege
		}
	}

	/** Better to deal with it */
	private fun handleNationDisband(id: Oid<Nation>) {
		getAllSieges().filter { it.attacker == id }.forEach {
			it.removeActive()
			it.fail(true)
		}
		getAllSieges().filter { it.defender == id }.forEach {
			unclaim(it.region.id)
			it.removeActive()
		}
	}

	private fun isInBigShip(player: Player): Boolean {
		val starship = ActiveStarships.findByPilot(player) ?: return false
		return starship.initialBlockCount >= MIN_SHIP_SIZE
	}

	fun unclaim(territoryId: Oid<DominionTerritory>) = Tasks.async {
		DominionTerritory.setNation(territoryId, null)
		recentlySiegedTerritories[territoryId] = System.currentTimeMillis()
		Notify.chatAndGlobal(template(
			text("{0} has been unclaimed!", HE_MEDIUM_GRAY),
			Regions.get<RegionDominionTerritory>(territoryId).name
		))
	}

	fun isWinner(siege: Oid<DominionTerritorySiegeData>, nation: Oid<Nation>): Boolean {
		val completed = DominionTerritorySiegeData.findOnePropById(siege, DominionTerritorySiegeData::complete)
		if (completed != true) return false

		val props = DominionTerritorySiegeData.findPropsById(siege, DominionTerritorySiegeData::attacker, DominionTerritorySiegeData::defender, DominionTerritorySiegeData::attackerPoints, DominionTerritorySiegeData::defenderPoints) ?: return false
		val attacker = props[DominionTerritorySiegeData::attacker]
		val attackerPoints = props[DominionTerritorySiegeData::attackerPoints]
		val defender = props[DominionTerritorySiegeData::defender]
		val defenderPoints = props[DominionTerritorySiegeData::defenderPoints]

		val success = attackerPoints > defenderPoints
		if (nation == attacker && success) return true
		if (nation == defender && !success) return true

		return false
	}
}
