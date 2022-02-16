package net.starlegacy.feature.starship.hyperspace

import java.io.File
import java.util.Locale
import net.starlegacy.SLComponent
import net.starlegacy.feature.starship.event.StarshipTranslateEvent
import net.starlegacy.util.d
import net.starlegacy.util.distanceSquared
import net.starlegacy.util.msg
import org.bukkit.World
import org.bukkit.configuration.file.FileConfiguration
import org.bukkit.configuration.file.YamlConfiguration
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerSet

object HyperspaceBeacons : SLComponent() {
	private const val BEACON_RADIUS = 200
	private const val BEACON_RADIUS_2 = BEACON_RADIUS * BEACON_RADIUS

	data class Beacon(val id: String, val world: String, val x: Int, val z: Int, val pairId: String, val schem: String)

	private val beacons = mutableMapOf<String, Beacon>()

	private val file = File(plugin.dataFolder, "hyperspace_beacon_data.yml")
	private lateinit var config: FileConfiguration

	override fun onEnable() {
		file.createNewFile()
		config = YamlConfiguration.loadConfiguration(file)
		loadBeacons()
		reloadDynmap()

		subscribe<StarshipTranslateEvent> { event ->
			val ship = event.ship
			val beacon = HyperspaceBeacons[ship.world, ship.centerOfMass.x, ship.centerOfMass.z]
			if (ship.nearbyBeacon != beacon) {
				ship.nearbyBeacon = beacon
				val pilot = event.player
				if (beacon != null) {
					pilot msg "&3Detected signal from hyperspace beacon&b ${beacon.id}&3" +
						", destination&b ${beacon.pairId}&3. Engage hyperdrive? &7&o(/usebeacon)"
				} else {
					pilot msg "&cExited beacon communication radius"
				}
			}
		}
	}

	fun save() {
		config.save(file)
	}

	operator fun get(id: String): Beacon? = beacons[id]

	private fun loadBeacons() {
		if (config.getConfigurationSection("beacons") == null) {
			config.createSection("beacons")
		}

		val keys = config.getConfigurationSection("beacons")?.getKeys(false) ?: return
		for (id in keys) {
			val world = config.getString("beacons.$id.world")?.lowercase(Locale.getDefault()) ?: return
			val x = config.getDouble("beacons.$id.x").toInt()
			val z = config.getDouble("beacons.$id.z").toInt()
			val pairId = config.getString("beacons.$id.pairId")
				?: config.getString("beacons.$id.pairedId")
				?: continue
			val schem = config.getString("beacons.$id.schem") ?: "HyperspaceBeacon"
			beacons[id] = Beacon(id, world, x, z, pairId, schem)
		}
	}

	fun reloadDynmap() {
		val api = DynmapPlugin.plugin.markerAPI
		var set: MarkerSet? = api.getMarkerSet("starships.hyperspace")

		set?.deleteMarkerSet()

		set = api.createMarkerSet("starships.hyperspace", "Hyperspace", null, false)

		for (beacon in beacons.values) {
			val split = beacon.id.split("_")

			val name = "${split[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }} System -> ${split[1].replaceFirstChar {
				if (it.isLowerCase()) it.titlecase(
					Locale.getDefault()
				) else it.toString()
			}} System"

			val x = beacon.x.toDouble()
			val z = beacon.z.toDouble()
			set.createMarker(beacon.id, name, beacon.world, x, 128.0, z, api.getMarkerIcon("portal"), false)
		}
	}

	fun add(id: String, world: String, x: Int, z: Int, pairId: String, schem: String, save: Boolean = true) {
		config.set("beacons.$id.world", world)
		config.set("beacons.$id.x", x)
		config.set("beacons.$id.z", z)
		config.set("beacons.$id.pairId", pairId)
		config.set("beacons.$id.schem", schem)
		if (save) {
			save()
			reloadDynmap()
		}
	}

	fun removeBeacon(id: String, save: Boolean = true) {
		config.set("beacons.$id", null)
		if (save) {
			save()
		}
	}

	operator fun get(world: World, x: Int, z: Int): Beacon? {
		val xd = x.d()
		val y = 128.0
		val zd = z.d()
		for (beacon in beacons.values) {
			if (world.name != beacon.world) {
				continue
			}
			val distanceSquared = distanceSquared(xd, y, zd, beacon.x.d(), y, beacon.z.d())
			if (distanceSquared < BEACON_RADIUS_2) {
				return beacon
			}
		}
		return null
	}

	fun getBeacons() = beacons.values
}
