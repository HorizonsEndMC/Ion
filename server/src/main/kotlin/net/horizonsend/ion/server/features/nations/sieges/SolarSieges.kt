package net.horizonsend.ion.server.features.nations.sieges

import net.horizonsend.ion.common.database.Oid
import net.horizonsend.ion.common.database.schema.nations.Nation
import net.horizonsend.ion.common.database.schema.nations.SolarSiegeData
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_MEDIUM_GRAY
import net.horizonsend.ion.common.utils.text.formatNationName
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionSolarSiegeZone
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.features.starship.event.StarshipSunkEvent
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.slPlayerId
import net.kyori.adventure.text.Component.text
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.Duration
import java.util.UUID
import kotlin.math.max
import kotlin.math.roundToInt

object SolarSieges : IonServerComponent() {
	private val onGoingSieges = mutableMapOf<Oid<SolarSiegeData>, SolarSiege>()

	fun getAllCurrentSieges(): Collection<SolarSiege> = onGoingSieges.values

	override fun onEnable() {
		tryLoadSieges()
		Tasks.asyncRepeat(60L, 60L) { getAllCurrentSieges().filter { it.needsSave }.forEach(SolarSiege::saveSiegeData) }
		Tasks.asyncRepeat(20L, 20L, ::processPassivePoints)
	}

	private fun tryLoadSieges() = SolarSiegeData.allIds().forEach(::loadPriorSiege)

	private fun loadPriorSiege(id: Oid<SolarSiegeData>) {
		val data = SolarSiegeData.findById(id) ?: return

		val region = Regions.get<RegionSolarSiegeZone>(data.zone)
		val siege = SolarSiege(
			id,
			region,
			data.attacker,
			data.attackerPoints,
			region.nation!!,
			data.attackerPoints
		)

		onGoingSieges[id] = siege
		Notify.chatAndGlobal(ofChildren(text("Resumed ", HE_MEDIUM_GRAY), siege.formatName()))
	}

	/**
	 * Initiates a new siege, assumes all checks have been done properly
	 **/
	fun initSiege(region: RegionSolarSiegeZone, attacker: Oid<Nation>): Boolean {
		val defender = checkNotNull(region.nation)
		val siegeData = SolarSiegeData.new(region.id, attacker)

		val siege = SolarSiege(
			siegeData,
			region,
			attacker = attacker,
			defender = defender,
		)

		onGoingSieges[siegeData] = siege
		Notify.chatAndEvents(template(
			text("{0} has initiated a siege on {1}'s solar siege zone in {2}", HE_MEDIUM_GRAY),
			formatNationName(attacker),
			formatNationName(defender),
			region.name
		))

		return true
	}

	data class ParticipationData(val player: UUID, val siege: Oid<SolarSiegeData>, val tagTime: Long)

	private val participationData = mutableMapOf<UUID, ParticipationData>()

	fun updateParticipation(player: Player, siege: SolarSiege) {
		participationData[player.uniqueId] = ParticipationData(player.uniqueId, siege.databaseId, System.currentTimeMillis())
	}

	private val PARTICIPATION_EXPIRATION = Duration.ofMinutes(10)

	fun getParticipating(player: Player): SolarSiege? {
		val participationData = participationData[player.uniqueId]
		if (participationData != null) {
			val now = System.currentTimeMillis()
			if (participationData.tagTime - now <= PARTICIPATION_EXPIRATION.toMillis()) return participationData.siege.let(onGoingSieges::get)
		}

		val contained = getAllCurrentSieges().firstOrNull { it.region.contains(player.location) } ?: return null
		updateParticipation(player, contained)
		return contained
	}

	@EventHandler
	fun onStarshipSink(event: StarshipSunkEvent) {
		val controller = event.previousController as? PlayerController ?: return
		processShipKill(controller)
	}

	private fun processShipKill(killedShipController: PlayerController) {
		val siege = getParticipating(killedShipController.player) ?: return
		val starship = killedShipController.starship
		val initPrintCost = starship.initPrintCost.roundToInt()

		val id = killedShipController.player.slPlayerId
		if (siege.isDefender(id)) siege.attackerPoints += initPrintCost
		if (siege.isAttacker(id)) siege.defenderPoints += initPrintCost
	}

	@EventHandler
	fun onPlayerDeath(event: PlayerDeathEvent) {
		processPlayerKill(event.player)
	}

	private fun processPlayerKill(player: Player) {
		val siege = getParticipating(player) ?: return

		val id = player.slPlayerId
		if (siege.isDefender(id)) siege.attackerPoints += 1000
		if (siege.isAttacker(id)) siege.defenderPoints += 1000
	}

	private fun processPassivePoints() {
		getAllCurrentSieges().forEach(::processPassivePoints)
	}

	private fun processPassivePoints(siege: SolarSiege) {
		val world = siege.region.bukkitWorld ?: return
		val starships = ActiveStarships.getInWorld(world)
		val contained = starships
			.filter { siege.region.contains(it.centerOfMass.x, it.centerOfMass.y, it.centerOfMass.z) }
			.mapNotNull { it.controller as? PlayerController }

		val defenderCount = contained.count { siege.isDefender(it.player.slPlayerId) }
		val attackerCount = contained.count { siege.isDefender(it.player.slPlayerId) }

		siege.defenderPoints += max(3, defenderCount) * pointTickValue // TODO formula
		siege.attackerPoints += max(3, attackerCount) * pointTickValue // TODO formula
	}

	// Constant
	private val pointTickValue = calculateTickValue()

	private fun calculateTickValue(): Int {
		val referenceDestroyerValue = 10000
		val durationSeconds = 90 /* Minutes */ * 60

		// Passive points are ticked once per second. Over the 90 minutes of the siege, the value of
		// 3 players contesting should be equal to the reference value of a sunk destroyer
		return (referenceDestroyerValue / durationSeconds) / 3
	}
}
