package net.horizonsend.ion.server.features.landsieges

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeTerritory
import net.horizonsend.ion.server.database.trx
import net.horizonsend.ion.server.features.landsieges.BeaconSiegeBattles.updateBeaconSieges
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import net.starlegacy.SLComponent
import net.starlegacy.cache.nations.NationCache
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import org.bukkit.block.Sign
import org.litote.kmongo.deleteOneById
import org.litote.kmongo.setValue
import java.time.DayOfWeek
import java.time.ZonedDateTime

object MoonSieges : SLComponent() {
	var siegeActive: Boolean = false
	val siegeDays = arrayOf(DayOfWeek.SATURDAY, DayOfWeek.SUNDAY)

	val activeBeaconBattles = mutableListOf<BeaconSiege>()

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
			updateMoonSieges()
			updateBeaconSieges()
		}

		Tasks.asyncRepeat(20, 20) {
			updateBeacons()
		}
	}

	fun updateMoonSieges() {
		val dayOfTheWeek = ZonedDateTime.now().dayOfWeek

		if (!siegeDays.contains(dayOfTheWeek)) {
			if (siegeActive) endSiegePeriod()

			siegeActive = false
			return
		}

		val isNew = !SiegeTerritory.all().any { it.siegeActive }

		if (isNew) beginSiegePeriod()


	}

	fun setAllSiegeStates(active: Boolean) {
		for (territory in SiegeTerritory.allIds()) {
			setSiegeState(territory, active)
		}
	}

	fun setSiegeState(territory: Oid<SiegeTerritory>, active: Boolean) = SiegeTerritory
		.updateById(territory, setValue(SiegeTerritory::siegeActive, active))


	fun beginSiegePeriod() {
		setAllSiegeStates(true)

		val notification = text().color(NamedTextColor.DARK_RED)
			.append(text("The siege period has begun."))
			.append(text("It will last for the next 48 hours", NamedTextColor.GRAY))
			.build()

		Notify.all(notification)
	}

	fun endSiegePeriod() {
		setAllSiegeStates(false)

		val notification = text().color(NamedTextColor.DARK_RED)
			.append(text("The siege period has ended."))
			.build()

		Notify.all(notification)

		clearBeacons()
	}

	fun updateBeacons() {
//		for (beacon in SiegeBeacon.all()) {
//			// Siege beacon period logic
//
//			val loc = beacon.location()
//			val sign = beacon.bukkitWorld().getBlockAt(loc).state as Sign
//
//			val (x, y, z) = beacon.vec3i() + SiegeBeaconMultiblock.getCenter(sign)
//			val location = Location(beacon.bukkitWorld(), x.toDouble(), y.toDouble(), z.toDouble())
//
//			beacon.laser?.stop()
//			beacon.laser = CrystalLaser(
//				location.add(0.0, 1.0 ,0.0),
//				location.add(0.0, 400.0, 0.0),
//				2,
//				400
//			)
//
//			beacon.laser!!.start(IonServer)
//
//			for (player in loc.getNearbyPlayers(25.0, 100.0, 25.0)) {
//
//			}
//		}
	}

	fun clearBeacons() {
		for (siegeBeacon in SiegeBeacon.all()) {
			trx { session ->
				SiegeBeacon.col.deleteOneById(session, siegeBeacon._id)
			}

			val win = siegeBeacon.points > 0

			val siegeTerritory = SiegeTerritory.findById(siegeBeacon.siegeTerritory) ?: continue
			val nation = NationCache[siegeBeacon.attacker]

			val message = text().color(NamedTextColor.RED).decoration(TextDecoration.BOLD, true)
				.append(text("The siege of "))
				.append(text(siegeTerritory.name, NamedTextColor.GOLD))
				.append(text(" by "))
				.append(text(nation.name, NamedTextColor.GOLD))

			if (win) message
				.append(text(" has failed."))
			else message
				.append(text(" has succeeded."))

			Notify.all(message.build())
		}
	}
}
