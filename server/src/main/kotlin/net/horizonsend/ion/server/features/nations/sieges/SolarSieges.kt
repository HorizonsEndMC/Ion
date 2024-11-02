package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.cache.nations.RelationCache
import net.horizonsend.ion.common.database.oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.NationRelation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeZone
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.discord.Embed
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.IonServer.nationsConfiguration
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Discord
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component.text
import org.apache.commons.lang3.time.TimeZones.GMT_ID
import org.bukkit.Bukkit
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.DayOfWeek
import java.time.LocalDate
import java.util.Calendar
import java.util.Date
import java.util.TimeZone.getTimeZone
import java.util.UUID
import java.util.concurrent.TimeUnit
import kotlin.math.roundToInt

object SolarSieges : IonServerComponent(true) {
	val config get() = nationsConfiguration.solarSiegeConfiguration

	@Synchronized
	private fun locked(block: () -> Unit) = block()

	private fun asyncLocked(block: () -> Unit) = Tasks.async {
		locked(block)
	}

	override fun onEnable() {
		tryLoadSieges()
		Tasks.asyncRepeat(60L, 60L) { getAllActiveSieges().filter { it.needsSave }.forEach(SolarSiege::saveSiegeData) }
		Tasks.asyncRepeat(20L * 60L, 20L * 60L, ::processPassivePoints)

		Nation.watchDeletes { handleNationDisband(it.oid) }
	}

	private val preparationSieges = mutableMapOf<Oid<SolarSiegeData>, SolarSiege>()
	private val activeSieges = mutableMapOf<Oid<SolarSiegeData>, SolarSiege>()

	private fun getAllPreparingSieges(): Collection<SolarSiege> = preparationSieges.values
	private fun getAllActiveSieges(): Collection<SolarSiege> = activeSieges.values

	fun getAllSieges(): Collection<SolarSiege> = getAllPreparingSieges().plus(getAllActiveSieges())

	fun setPreparing(siege: SolarSiege) {
		preparationSieges[siege.databaseId] = siege
	}

	fun setActive(siege: SolarSiege) {
		preparationSieges.remove(siege.databaseId)
		activeSieges[siege.databaseId] = siege
	}

	fun removeActive(siege: SolarSiege) {
		preparationSieges.remove(siege.databaseId) // Just in case
		activeSieges.remove(siege.databaseId)
	}

	/**
	 * Returns whether this siege zone is being prepared for a siege, or actively under siege
	 **/
	private fun isUnderSiege(stationId: Oid<SolarSiegeZone>): Boolean {
		return activeSieges.any { it.value.region.id == stationId } ||
			   preparationSieges.any { it.value.region.id == stationId }
	}

	/**
	 * Returns whether the current time is inside the period when a siege can be declared
	 **/
	private fun isSiegeDeclarationPeriod(): Boolean {
		val localDate = LocalDate.now()
		if (!(localDate.dayOfWeek == DayOfWeek.SATURDAY || localDate.dayOfWeek == DayOfWeek.SUNDAY)) return false

		val calendar = Calendar.getInstance(getTimeZone(GMT_ID))
		calendar.time = Date()
		val hour = calendar.get(Calendar.HOUR_OF_DAY)
		return hour in 14..16
	}

	// Initiation, Ending
	fun initSiege(player: Player) = asyncLocked {
		val nation = PlayerCache[player].nationOid
			?: return@asyncLocked player.userError("You need to be in a nation to siege a zone.")

		val zone = Regions.findFirstOf<RegionSolarSiegeZone>(player.location)
			?: return@asyncLocked player.userError("You must be within a zone's area to siege it.")

		if (zone.nation?.let { RelationCache[nation, it] >= NationRelation.Level.ALLY } == true) {
			return@asyncLocked player.userError("This zone is owned by an ally or your nation!")
		}

		val stationId = zone.id

		if (isUnderSiege(stationId)) {
			return@asyncLocked player.userError("This station is already under siege!")
		}

		if (zone.nation == nation) {
			return@asyncLocked player.userError("Your nation already owns this station.")
		}

		if (!isSiegeDeclarationPeriod() && false) {
			player.userError("It is not the siege declaration period!")
			player.information("Solar Sieges can only be declared on Saturday or Sunday between 14:00 and 17:00 UTC")
			return@asyncLocked
		}

		val oldNation = zone.nation

		if (oldNation == null) {
			SolarSiegeZone.setNation(zone.id, nation)
			Notify.chatAndEvents(template(
				text("The solar siege zone in {0} has been captured by {1} of {2}"),
				useQuotesAroundObjects = false,
				zone.world,
				player.name,
				NationCache[nation].name
			))
			player.information("Captured siege zone, since it had no owner.")
			return@asyncLocked
		}

		initSiege(zone, player.name, nation)
	}

	/**
	 * Initiates a new siege, assumes all checks have been done properly
	 **/
	private fun initSiege(region: RegionSolarSiegeZone, attackerName: String, attackerNation: Oid<Nation>): Boolean {
		val defender = checkNotNull(region.nation)
		val siegeData = SolarSiegeData.new(region.id, attackerNation, defender)

		val siege = SolarSiege(
			siegeData,
			region,
			attacker = attackerNation,
			defender = defender,
			declaredTime = System.currentTimeMillis()
		)

		Notify.chatAndGlobal(template(
			text("{0} of {1} has initiated a siege on {2}'s solar siege zone in {3}. The preparation period begins now.", HE_MEDIUM_GRAY),
			attackerName,
			formatNationName(attackerNation),
			formatNationName(defender),
			region.name
		))

		Discord.sendEmbed(IonServer.discordSettings.eventsChannel, Embed(
			title = "Siege Declaration",
			description = "$attackerName of ${formatNationName(attackerNation).plainText()} has declared a siege of ${formatNationName(defender).plainText()}'s Solar Siege Zone in " +
				"${region.world}. The siege will start <t:${TimeUnit.MILLISECONDS.toSeconds(siege.getActivePeriodStart())}:R>."
		))

		siege.scheduleTasks()

		return true
	}

	fun attackerAbandonSiege(player: Player, siege: SolarSiege) {
		val playerNation = PlayerCache[player].nationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = NationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}"),
			useQuotesAroundObjects = false,
			player.name,
			nationName,
			siege.formatName(),
		))

		siege.fail()
	}

	fun defenderAbandonSiege(player: Player, siege: SolarSiege) {
		val playerNation = PlayerCache[player].nationOid ?: return

		siege.isAbandoned = true
		siege.removeActive()

		val nationName = NationCache[playerNation].name
		Notify.chatAndEvents(template(
			text("{0} of {1} has abandoned {2}"),
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
			log.info("Awarded attacker $points points")

			IonServer.server.sendMessage(template(
				text("{0} accrued {1} points for killing {2}."),
				formatNationName(siege.attacker),
				points,
				player.name
			))
		}

		if (siege.isDefender(killer.slPlayerId) && siege.isAttacker(player.slPlayerId)) {
			siege.defenderPoints += points
			log.info("Awarded defender $points points")

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

	private fun processPassivePoints(siege: SolarSiege) {
		val world = siege.region.bukkitWorld ?: return
		val starships = ActiveStarships.getInWorld(world)
		val contained = starships
			.filter { siege.region.contains(it.centerOfMass.x, it.centerOfMass.y, it.centerOfMass.z) }
			.mapNotNull { it.controller as? PlayerController }

		val siegeAudience = ForwardingAudience { Bukkit.getOnlinePlayers().filter { getParticipating(it) == siege } }

		val defenderCount = contained.count { siege.isDefender(it.player.slPlayerId) }
		log.info("$defenderCount defender ships present")
		val defenderNew = defenderCount.coerceAtMost(3) * pointTickValue

		if (defenderNew > 0) {
			siege.defenderPoints += defenderNew
			log.info("Awarded defender $defenderNew points")

			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the siege region."),
				formatNationName(siege.defender),
				defenderNew
			))
		}

		val attackerCount = contained.count { siege.isAttacker(it.player.slPlayerId) }
		log.info("$attackerCount attacker ships present")
		val attackerNew = attackerCount.coerceAtMost(3) * pointTickValue

		if (attackerNew > 0) {
			siege.attackerPoints += attackerNew
			log.info("Awarded attacker $attackerNew points")

			siegeAudience.sendMessage(template(
				text("{0} accrued {1} passive points for being inside the siege region."),
				formatNationName(siege.attacker),
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
	data class ParticipationData(val player: UUID, val siege: Oid<SolarSiegeData>, val tagTime: Long)

	private val participationData = mutableMapOf<UUID, ParticipationData>()

	private fun updateParticipation(player: Player, siege: SolarSiege) {
		participationData[player.uniqueId] = ParticipationData(player.uniqueId, siege.databaseId, System.currentTimeMillis())
	}

	private fun getParticipating(player: Player): SolarSiege? {
		val participationData = participationData[player.uniqueId]
		if (participationData != null) {
			val now = System.currentTimeMillis()
			val participationLength = config.participationLength.toDuration()
			if (participationData.tagTime - now <= participationLength.toMillis()) return participationData.siege.let(activeSieges::get)
		}

		val contained = getAllActiveSieges().firstOrNull { it.region.contains(player.location) && it.isActivePeriod() } ?: return null
		updateParticipation(player, contained)
		return contained
	}

	// Load from restart
	private fun tryLoadSieges() = SolarSiegeData.findActive().map(SolarSiegeData::_id).forEach(::loadPriorSiege)

	private fun loadPriorSiege(id: Oid<SolarSiegeData>) = asyncLocked {
		val data = SolarSiegeData.findById(id) ?: return@asyncLocked

		val region = Regions.get<RegionSolarSiegeZone>(data.zone)
		val siege = SolarSiege(
			id,
			region,
			data.attacker,
			data.attackerPoints,
			region.nation!!,
			data.attackerPoints,
			SolarSiegeData.findPropById(id, SolarSiegeData::declareTime)!!.time
		)

		log.info("Resuming solar siege in ${region.world}")

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
		getAllSieges().filter { it.defender == id }.forEach { it.fail(); it.removeActive() }
		getAllSieges().filter { it.attacker == id }.forEach { it.succeed(); it.removeActive() }
	}
}
