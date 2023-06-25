package net.horizonsend.ion.server.features.landsieges

import fr.skytasul.guardianbeam.Laser.CrystalLaser
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.database.Oid
import net.horizonsend.ion.server.database.schema.misc.SLPlayerId
import net.horizonsend.ion.server.database.schema.nations.Nation
import net.horizonsend.ion.server.database.schema.nations.moonsieges.ForwardOperatingBase
import net.horizonsend.ion.server.database.schema.nations.moonsieges.MoonSiege
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeBeacon
import net.horizonsend.ion.server.database.schema.nations.moonsieges.SiegeTerritory
import net.horizonsend.ion.server.features.landsieges.BeaconSiege.updateBeaconSieges
import net.horizonsend.ion.server.features.multiblock.moonsiege.SiegeBeaconMultiblock
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.SLComponent
import net.starlegacy.util.Notify
import net.starlegacy.util.Tasks
import org.bukkit.Location
import org.bukkit.block.Sign
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
			checkMoonSiegeWin()
			updateBeaconSieges()
		}

		Tasks.asyncRepeat(20, 20) {
			updateBeacons()
		}
	}

	fun updateMoonSieges() {
		val dayOfTheWeek = ZonedDateTime.now().dayOfWeek

		if (!siegeDays.contains(dayOfTheWeek)) {
			siegeActive = false
			checkMoonSiegeWin()
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

		// List of changed hands
		val notification = text().color(NamedTextColor.DARK_RED)
			.append(text("The siege period has ended."))
			.build()

		Notify.all(notification)
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

	fun endMoonSiege(siegeId: Oid<MoonSiege>) {
		val siege = MoonSiege.findById(siegeId) ?: return
		val territory = SiegeTerritory.findById(siege.siegeTerritory) ?: error("Siege territory ${siege.siegeTerritory} is not saved!")

		// Clear FOB nations
		for (forwardOperatingBase in ForwardOperatingBase.get(territory.bukkitWorld())) {
			ForwardOperatingBase.updateById(forwardOperatingBase._id, setValue(ForwardOperatingBase::nation, null))
		}
	}

}
