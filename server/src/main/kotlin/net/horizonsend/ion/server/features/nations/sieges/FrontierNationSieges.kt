package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.FrontierNationCache
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.FrontierNation
import net.horizonsend.ion.common.database.schema.nations.FrontierNationSiegeData
import net.horizonsend.ion.common.database.schema.nations.FrontierTerritory
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatFrontierNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.chat.Discord
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionFrontierTerritory
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
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object FrontierNationSieges : IonServerComponent(true) {
	val config get() = ConfigurationFiles.nationConfiguration().frontierNationSiegeConfiguration

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	override fun onEnable() {
		tryLoadSieges()
		Tasks.asyncRepeat(60L, 60L) { getAllActiveSieges().filter { it.needsSave }.forEach(FrontierNationSiege::saveSiegeData) }
		Tasks.asyncRepeat(20L * 60L, 20L * 60L, ::processPassivePoints)

		FrontierNation.watchDeletes { handleNationDisband(it.oid) }
	}

	private val preparationSieges = mutableMapOf<Oid<FrontierNationSiegeData>, FrontierNationSiege>()
	private val activeSieges = mutableMapOf<Oid<FrontierNationSiegeData>, FrontierNationSiege>()

	private fun getAllPreparingSieges(): Collection<FrontierNationSiege> = preparationSieges.values
	private fun getAllActiveSieges(): Collection<FrontierNationSiege> = activeSieges.values

	fun getAllSieges(): Collection<FrontierNationSiege> = getAllPreparingSieges().plus(getAllActiveSieges())

	fun setPreparing(siege: FrontierNationSiege) {
		preparationSieges[siege.databaseId] = siege
	}

	fun setActive(siege: FrontierNationSiege) {
		preparationSieges.remove(siege.databaseId)
		activeSieges[siege.databaseId] = siege
	}

	fun removeActive(siege: FrontierNationSiege) {
		preparationSieges.remove(siege.databaseId) // Just in case
		activeSieges.remove(siege.databaseId)
	}

	/**
	 * Returns whether this siege zone is being prepared for a siege, or actively under siege
	 **/
	private fun isUnderSiege(stationId: Oid<FrontierTerritory>): Boolean {
		return activeSieges.any { it.value.region.id == stationId } || preparationSieges.any { it.value.region.id == stationId }
	}

	/**
	 * Returns whether the current territory belongs to a nation that has below 20 power
	 **/
	private fun canBeSieged(nation: Oid<FrontierNation>): Boolean {
		return FrontierNationCache[nation].siegable
	}

	// Initiation, Ending
	fun initSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].frontierNationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a zone.")


		//TODO fix
		val zone = Regions.findFirstOf<RegionFrontierTerritory>(player.location)
			?: return@asyncLocked player.userError("You must be within a zone's area to siege it.")

		val stationId = zone.id

		if (isUnderSiege(stationId)) {
			return@asyncLocked player.userError("This station is already under siege!")
		}

		if (zone.frontierNation == nation) {
			return@asyncLocked player.userError("Your nation already owns this station.")
		}

		val oldNation = zone.frontierNation ?: return@asyncLocked player.userError("This territory cannot be besieged!")

		if (!canBeSieged(zone.frontierNation!!)) {
			player.userError("This territory is not siege able!")
			player.information("You can only siege a nation that has too low power!")
			return@asyncLocked
		}

		initSiege(zone, player.name, nation)
	}

	/**
	 * Initiates a new siege, assumes all checks have been done properly
	 **/
	private fun initSiege(region: RegionFrontierTerritory, attackerName: String, attackerNation: Oid<FrontierNation>): Boolean {
		val defender = checkNotNull(region.frontierNation)
		val siegeData = FrontierNationSiegeData.new(region.id, attackerNation, defender)

		val siege = FrontierNationSiege(
			siegeData,
			region,
			attacker = attackerNation,
			defender = defender,
			declaredTime = System.currentTimeMillis()
		)

		Notify.chatAndGlobal(template(
			text("{0} of {1} has initiated a siege on {2}'s frontier nation in {3}. The preparation period begins now.", HE_MEDIUM_GRAY),
			attackerName,
			formatFrontierNationName(attackerNation),
			formatFrontierNationName(defender),
			region.name
		))

		Discord.sendEmbed(ConfigurationFiles.discordSettings().eventsChannel, Embed(
			title = "Siege Declaration",
			description = "$attackerName of ${formatFrontierNationName(attackerNation).plainText()} has declared a siege of ${formatFrontierNationName(defender).plainText()}'s Frontier Nation in " +
				"${region.world}. The siege will start <t:${TimeUnit.MILLISECONDS.toSeconds(siege.getActivePeriodStart())}:R>."
		))

		siege.scheduleTasks()

		return true
	}

	fun attackerAbandonSiege(player: Player, siege: FrontierNationSiege) {
		val playerNation = PlayerCache[player].frontierNationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = FrontierNationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}", HE_MEDIUM_GRAY),
			useQuotesAroundObjects = false,
			player.name,
			nationName,
			siege.formatName(),
		))

		siege.fail()
	}

	fun defenderAbandonSiege(player: Player, siege: FrontierNationSiege) {
		val playerNation = PlayerCache[player].frontierNationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = FrontierNationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}", HE_MEDIUM_GRAY),
			useQuotesAroundObjects = false,
			player.name,
			nationName,
			siege.formatName(),
		))

		siege.succeed()
	}

	// Point accrual
	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		val damager = event.starship.damagers
			.filter { it.key is PlayerDamager }
			.maxByOrNull { it.value.points.get() }?.key as? PlayerDamager ?: return

		val initPrintCost = (event.starship.initPrintCost * config.shipCostMultiplier).roundToInt()

		processKill(controller.player, damager.player, initPrintCost)
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		val killer = event.player.killer ?: return
		processKill(event.player, killer, config.playerKillPoints)
	}

	private fun processKill(player: Player, killer: Player, points: Int) {
		val siege = getParticipating(player) ?: return
		if (getParticipating(killer) != siege) return // Weird case, probably unrelated player
		if (!killer.isOnline) return

		if (siege.isDefender(player.slPlayerId) && siege.isAttacker(killer.slPlayerId)) {
			siege.attackerPoints += points
			log.info("Awarded attacker $points points for killing ${player.name}")

			IonServer.server.sendMessage(template(
				text("{0} accrued {1} points for killing {2}."),
				formatFrontierNationName(siege.attacker),
				points,
				player.name
			))
		}

		if (siege.isDefender(killer.slPlayerId) && siege.isAttacker(player.slPlayerId)) {
			siege.defenderPoints += points
			log.info("Awarded defender $points points for killing ${player.name}")

			IonServer.server.sendMessage(template(
				text("{0} accrued {1} points for killing {2}."),
				formatFrontierNationName(siege.defender),
				points,
				player.name
			))
		}
	}

	private fun processPassivePoints() {
		getAllActiveSieges().forEach(::processPassivePoints)
	}

	private fun processPassivePoints(siege: FrontierNationSiege) {
		val world = siege.region.bukkitWorld ?: return
		val starships = ActiveStarships.getInWorld(world)
		val contained = starships
			.filter { siege.region.contains(it.centerOfMass.x, it.centerOfMass.y, it.centerOfMass.z) && it.initialBlockCount >= config.minimumPassivePointsShipSize }
			.mapNotNull { it.controller as? PlayerController }

		val siegeAudience = ForwardingAudience { Bukkit.getOnlinePlayers().filter { getParticipating(it) == siege } }

		val defenderCount = contained.count { siege.isDefender(it.player.slPlayerId) }
		log.info("$defenderCount defender ships present")
		val defenderNew = defenderCount.coerceAtMost(3) * pointTickValue

		if (defenderNew > 0) {
			siege.defenderPoints += defenderNew
			log.info("Awarded defender $defenderNew passive points")

			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the frontier nation territory."),
				formatFrontierNationName(siege.defender),
				defenderNew
			))
		}

		val attackerCount = contained.count { siege.isAttacker(it.player.slPlayerId) }
		log.info("$attackerCount attacker ships present")
		val attackerNew = attackerCount.coerceAtMost(3) * pointTickValue

		if (attackerNew > 0) {
			siege.attackerPoints += attackerNew
			log.info("Awarded attacker $attackerNew passive points")

			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the frontier nation territory."),
				formatFrontierNationName(siege.attacker),
				attackerNew
			))
		}
	}

	// Constant
	private val pointTickValue = calculateTickValue()

	private fun calculateTickValue(): Int {
		val referenceDestroyerValue = (config.referenceDestroyerPrice * config.shipCostMultiplier).roundToInt()
		val durationMinutes = config.activeWindowDuration.toDuration().toMinutes().toInt()

		// Passive points are ticked once per second. Over the 90 minutes of the siege, the value of
		// 3 players contesting should be equal to the reference value of a sunk destroyer
		return (referenceDestroyerValue / durationMinutes) / 3
	}

	// Participation utils
	data class ParticipationData(val player: UUID, val siege: Oid<FrontierNationSiegeData>, val tagTime: Long)

	private val participationData = mutableMapOf<UUID, ParticipationData>()

	private fun updateParticipation(player: Player, siege: FrontierNationSiege) {
		participationData[player.uniqueId] = ParticipationData(player.uniqueId, siege.databaseId, System.currentTimeMillis())
	}

	private fun getParticipating(player: Player): FrontierNationSiege? {
		val participationData = participationData[player.uniqueId]
		if (participationData != null) {
			val now = System.currentTimeMillis()
			val participationLength = config.participationLength.toDuration()
			if (participationData.tagTime - now <= participationLength.toMillis()) {
				if (activeSieges.containsKey(participationData.siege)) return participationData.siege.let(activeSieges::get)
			}
		}

		val contained = getAllActiveSieges().firstOrNull { it.region.contains(player.location) && it.isActivePeriod() } ?: return null
		updateParticipation(player, contained)
		return contained
	}

	// Load from restart
	private fun tryLoadSieges() = FrontierNationSiegeData.findActive().map(FrontierNationSiegeData::_id).forEach(::loadPriorSiege)

	private fun loadPriorSiege(id: Oid<FrontierNationSiegeData>) = asyncLocked {
		val data = FrontierNationSiegeData.findById(id) ?: return@asyncLocked

		val region = Regions.get<RegionFrontierTerritory>(data.zone)
		val siege = FrontierNationSiege(
			id,
			region,
			data.attacker,
			data.attackerPoints,
			data.defender,
			data.defenderPoints,
			FrontierNationSiegeData.findPropById(id, FrontierNationSiegeData::declareTime)!!.time
		)

		log.info("Resuming frontier nation siege in ${region.world}")

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
	private fun handleNationDisband(id: Oid<FrontierNation>) {
		getAllSieges().filter { it.defender == id }.forEach { it.fail(); it.removeActive() }
		getAllSieges().filter { it.attacker == id }.forEach { it.succeed(); it.removeActive() }
	}


	fun isWinner(siege: Oid<FrontierNationSiegeData>, nation: Oid<FrontierNation>): Boolean {
		val completed = FrontierNationSiegeData.findOnePropById(siege, FrontierNationSiegeData::complete)
		if (completed != true) return false

		val props = FrontierNationSiegeData.findPropsById(siege, FrontierNationSiegeData::attacker, FrontierNationSiegeData::defender, FrontierNationSiegeData::attackerPoints, FrontierNationSiegeData::defenderPoints) ?: return false
		val attacker = props[FrontierNationSiegeData::attacker]
		val attackerPoints = props[FrontierNationSiegeData::attackerPoints]
		val defender = props[FrontierNationSiegeData::defender]
		val defenderPoints = props[FrontierNationSiegeData::defenderPoints]

		val success = attackerPoints > defenderPoints
		if (nation == attacker && success) return true
		if (nation == defender && !success) return true

		return false
	}
}
