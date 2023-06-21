package net.horizonsend.ion.server.features.landsieges

import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.NationRelation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.features.multiblock.moonsiege.SiegeBeaconMultiblock
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.PlayerCache
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.nations.region.Regions
import net.starlegacy.feature.nations.region.types.RegionSiegeTerritory
import net.starlegacy.feature.starship.event.StarshipExplodeEvent
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import net.starlegacy.util.Vec3i
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.entity.PlayerDeathEvent
import java.time.DayOfWeek
import java.time.ZonedDateTime

object MoonSieges : SLComponent() {
	var siegeActive: Boolean = false
	val siegeDays = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

	data class BeaconSiege(
		val initiate: SLPlayerId,
		val beacon: Sign,
		var points: Int,
	)

	override fun onEnable() {
		// Update siges on the main server only
		if (IonServer.configuration.serverName != "Survival") return

		Tasks.syncRepeat(20, 20) {
			updateSiegePeriod()
		}

		Tasks.asyncRepeat(20, 20) {
			updateBeacons()
		}
	}

	fun updateBeacons() {
		for (beacon in SiegeBeacon.all()) {
			val loc = beacon.location()
			val sign = beacon.bukkitWorld().getBlockAt(loc).state as Sign

			val (x, y, z) = beacon.vec3i() + SiegeBeaconMultiblock.getCenter(sign)


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

	fun tryBeginSiege(player: Player, beacon: Sign) {
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

	fun endSiege() {

	}

	@EventHandler
	fun onPlayerKill(event: PlayerDeathEvent) {

	}

	@EventHandler
	fun onShipKill(event: StarshipExplodeEvent) {

	}
}
