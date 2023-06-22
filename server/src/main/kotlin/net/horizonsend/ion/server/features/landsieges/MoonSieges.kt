package net.horizonsend.ion.server.features.landsieges

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.ForwardOperatingBase
import net.horizonsend.ion.server.database.schema.nations.moonsieges.MoonSiege
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeTerritory
import net.horizonsend.ion.server.features.multiblock.moonsiege.SiegeBeaconMultiblock
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.audience.ForwardingAudience
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextColor
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.distance
import net.starlegacy.util.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import org.litote.kmongo.eq
import org.litote.kmongo.setValue
import java.time.DayOfWeek
import java.time.ZonedDateTime

object MoonSieges : SLComponent() {
	var siegeActive: Boolean = false
	val siegeDays = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

	/**  **/
	val activeSieges = mutableMapOf<Oid<SiegeTerritory>, Oid<MoonSiege>>()

	val activeBeaconBattles = mutableListOf<BeaconSiege>()

	private const val BEACON_DETECTION_RADIUS = 100
	private const val BEACON_SIEGE_MAX_TIME_MS: Long = 1000 * 60 * 60 * 2

		/** Stores information for an individual battle for a beacon **/
	data class BeaconSiege(
		val initiate: SLPlayerId,
		val initiateNation: Oid<Nation>,

		val beacon: Sign,
		var points: Int,
		val startTime: Long = System.currentTimeMillis()
	)

	override fun onEnable() {
		// Update siges on the main server only
		if (IonServer.configuration.serverName != "Survival") return

		Tasks.syncRepeat(20, 20) {
			updateSiegePeriod()
			checkMoonSiegeWin()
			updateBeaconSieges()
		}

		Tasks.asyncRepeat(20, 20) {
			updateBeacons()
		}
	}

	fun updateBeaconSieges() {
		val finishedBeaconSieges = mutableListOf<BeaconSiege>()

		for (beaconSiege in activeBeaconBattles) {
			if (System.currentTimeMillis() - beaconSiege.startTime < BEACON_SIEGE_MAX_TIME_MS) continue

			finishedBeaconSieges.add(beaconSiege)

			val beaconName = (beaconSiege.beacon.line(3) as TextComponent).content()

			Notify online text(
				"Siege Beacon Battle for $beaconName on ${beaconSiege.beacon.world} has ended.",
				TextColor.fromHexString(" \t#EC5800")
			)
		}

		activeBeaconBattles.removeAll(finishedBeaconSieges)
	}

	fun updateBeacons() {


		for (beacon in SiegeBeacon.all()) {
			// Siege beacon period logic

			val loc = beacon.location()
			val sign = beacon.bukkitWorld().getBlockAt(loc).state as Sign

			val (x, y, z) = beacon.vec3i() + SiegeBeaconMultiblock.getCenter(sign)
			val location = Location(beacon.bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())

			beacon.laser?.stop()
			beacon.laser = CrystalLaser(
				location.add(0.0, 1.0 ,0.0),
				location.add(0.0, 400.0, 0.0),
				2,
				400
			)

			beacon.laser!!.start(IonServer)


		}
	}

	fun checkMoonSiegeWin() { //TODO
		for (siegeTerritory in SiegeTerritory.all()) {
			val siege = MoonSiege.findOne(MoonSiege::siegeTerritory eq siegeTerritory._id) ?: continue

			val id = siegeTerritory._id

			val beacons = SiegeBeacon.getBeacons(id)

			if (beacons.all {  })
		}
	}

	fun updateSiegePeriod() {
		val dayOfTheWeek = ZonedDateTime.now().dayOfWeek

		if (!siegeDays.contains(dayOfTheWeek)) {
			siegeActive = false
			return
		}

		siegeActive = true

		val notification = Component.text().color(NamedTextColor.DARK_RED)
			.append(Component.text("The siege period has begun."))
			.append(Component.text("It will last for the next 48 hours", NamedTextColor.GRAY))
			.build()

		Notify.all(notification)
	}

	fun tryBeginBeaconSiege(player: Player, beacon: Sign) {
		val nation = PlayerCache[player].nationOid
			?: return player.userError("You need to be in a nation to siege a station.")

		val siegeTerritory = Regions.findFirstOf<RegionSiegeTerritory>(player.location)
			?: return player.userError("You must be within a station's area to siege it.")

		if (siegeTerritory.nation?.let { NationRelation.getRelationActual(nation, it).ordinal >= NationRelation.Level.ALLY.ordinal } == true) {
			return player.userError("This station is owned by an ally of your nation.")
		}

		(Multiblocks[beacon] as? SiegeBeaconMultiblock)?.onSignInteract(beacon, player) ?: return

		//TODO
	}

	fun endMoonSiege(siegeId: Oid<MoonSiege>) {
		val siege = MoonSiege.findById(siegeId) ?: return
		val territory = SiegeTerritory.findById(siege.siegeTerritory) ?: error("Siege territory ${siege.siegeTerritory} is not saved!")

		// Clear FOB nations
		for (forwardOperatingBase in ForwardOperatingBase.get(territory.bukkitWorld())) {
			ForwardOperatingBase.updateById(forwardOperatingBase._id, setValue(ForwardOperatingBase::nation, null))
		}
	}

	/** Used for detecting ship or player kills **/
	fun getBeacon(location: Location): SiegeBeacon? {
		val (x, y, z) = Vec3i(location)

		return SiegeBeacon.all().asSequence()
			.filter { it.world == location.world.name }
			.filter { distance(it.x, it.y, it.z, x, y, z) <= BEACON_DETECTION_RADIUS }
			.firstOrNull()
	}

	@EventHandler
	fun onPlayerKill(event: PlayerDeathEvent) {
		val beacon = getBeacon(event.player.location) ?: return
		event.player.killer ?: return

		// Determine the siege and the siege nations, do an ally check, determine points
	}

	@EventHandler
	fun onShipKill(event: StarshipExplodeEvent) {
		val beacon = getBeacon(event.starship.centerOfMass.toLocation(event.starship.serverLevel.world)) ?: return
		val damagers = event.starship.damagers

		val damagerPlayers = damagers.filter {
			val slPlayer = PlayerCache[it.key.id]

			// Only check relation if the beacon has an owner
			val owner = beacon.owner?.let {
				val relation = NationRelation.getRelationActual(beacon.owner, slPlayer.nationOid ?: return@filter false)

				if (relation.ordinal >= NationRelation.Level.ALLY.ordinal) return@filter false
			}

			true
		}.mapNotNull { Bukkit.getPlayer(it.key.id) }
		// Determine the siege and the siege nations, do an ally check, determine points


	}

}
