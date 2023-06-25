package net.horizonsend.ion.server.features.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.features.multiblock.moonsiege.SiegeBeaconMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.TextComponent
import net.kyori.adventure.text.format.TextColor
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.util.Notify
import net.starlegacy.util.Vec3i
import net.starlegacy.util.distance
import net.starlegacy.util.toLocation
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent

object BeaconSiege : SLComponent() {
	private const val BEACON_DETECTION_RADIUS = 100
	private const val BEACON_SIEGE_MAX_TIME_MS: Long = 1000 * 60 * 60 * 2

	fun updateBeaconSieges() {
		val finishedBeaconSieges = mutableListOf<MoonSieges.BeaconSiege>()

		for (beaconSiege in MoonSieges.activeBeaconBattles) {
			if (System.currentTimeMillis() - beaconSiege.startTime < BEACON_SIEGE_MAX_TIME_MS) continue

			finishedBeaconSieges.add(beaconSiege)

			val beaconName = (beaconSiege.beacon.line(3) as TextComponent).content()

			Notify online Component.text(
				"Siege Beacon Battle for $beaconName on ${beaconSiege.beacon.world} has ended.",
				TextColor.fromHexString(" \t#EC5800")
			)
		}

		MoonSieges.activeBeaconBattles.removeAll(finishedBeaconSieges)
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

	/** Used for detecting ship or player kills **/
	fun getBeacon(location: Location): SiegeBeacon? {
		val (x, y, z) = Vec3i(location)

		return SiegeBeacon.all().asSequence()
			.filter { it.world == location.world.name }
			.filter { distance(it.x, it.y, it.z, x, y, z) <= MoonSieges.BEACON_DETECTION_RADIUS }
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
