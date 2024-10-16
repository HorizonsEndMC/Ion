package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.nations.NationsMap.dynmapLoaded
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.getLocationNear
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.TextColor
import org.bukkit.Bukkit
import org.bukkit.Location
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerAPI
import org.slf4j.Logger
import java.time.Duration
import java.util.function.Supplier
import kotlin.random.Random

/**
 * Creates a locus spawner scheduler. The spawner will be executed at a higher rate for set period, near a specific location.
 **/
class LocusScheduler(
	private val displayName: Component,
	private val dynmapColor: TextColor,
	private val duration: Supplier<Duration>,
	private val separation: Supplier<Duration>,
	private val announcementMessage: Component?,
	private val endMessage: Component?,
	private val radius: Double,
	private val spawnSeparation: Supplier<Duration>,
	private val worlds: List<String>
) : SpawnerScheduler, TickedScheduler {
	private lateinit var spawner: AISpawner

	override fun getSpawner(): AISpawner {
		return spawner
	}

	override fun setSpawner(spawner: AISpawner): SpawnerScheduler {
		this.spawner = spawner
		return this
	}

	var active: Boolean = false
	lateinit var center: Location

	private var lastActiveTime = System.currentTimeMillis()
	private var lastDuration: Duration = duration.get()

	override fun tick(logger: Logger) {
		if (!active) {
			// Interval from the end of the last one
			val interval = System.currentTimeMillis() - (lastActiveTime + lastDuration.toMillis())
			// Start the locus if the separation has passed
			if (interval > separation.get().toMillis()) start()
		} else {
			val interval = System.currentTimeMillis() - (lastActiveTime)

			// If active, check for count down, and trigger the spawners
			if (interval >= lastDuration.toMillis()) {
				end()
				return
			}

			// If not ended, tick spawner
			tickSpawner(logger)
		}
	}

	fun start() {
		lastActiveTime = System.currentTimeMillis()
		lastDuration = duration.get()

		center = calculateNewCenter()
		active = true
		markDynmapZone()
		if (announcementMessage != null) IonServer.server.sendMessage(template(
			announcementMessage,
			paramColor = HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			center.world.name,
			center.blockX,
			center.blockY,
			center.blockZ
		))
	}

	fun end() {
		active = false
		removeDynmapZone()
		if (endMessage != null) IonServer.server.sendMessage(endMessage)
	}

	private var spawnerLastExecuted: Long = System.currentTimeMillis()
	private var lastSpawnSeparation = spawnSeparation.get()

	private fun tickSpawner(logger: Logger) {
		val interval = System.currentTimeMillis() - spawnerLastExecuted
		if (interval < lastSpawnSeparation.toMillis()) return

		if (!isOccupied()) return

		spawnerLastExecuted = System.currentTimeMillis()
		lastSpawnSeparation = spawnSeparation.get()
		getSpawner().trigger(logger, AISpawningManager.context)
	}

	private fun calculateNewCenter(): Location {
		// If you make a world with nothing but a planet in a tiny world border this will crash your server, but that is on you
		val world = Bukkit.getWorld(worlds.random())!!
		val border = world.worldBorder

		val planets = Space.getAllPlanets()
			.filter { it.spaceWorld?.uid == world.uid }
			.plus(Space.getStars().filter { it.spaceWorld?.uid == world.uid })

		val borderRadius = border.size / 2.0
		val minX = border.center.x - borderRadius + radius
		val maxX = border.center.x + borderRadius - radius
		val minZ = border.center.z - borderRadius + radius
		val maxZ = border.center.z + borderRadius - radius

		var newLoc: Location? = null

		while (newLoc == null) {
			val newX = Random.nextDouble(minX, maxX)
			val newZ = Random.nextDouble(minZ, maxZ)

			if (planets.any { it.location.distance(Vec3i(newX.toInt(), it.location.y, newZ.toInt())) < 1000.0 }) continue

			newLoc = Location(world, newX, LOCUS_Y, newZ)
		}

		return newLoc
	}

	private fun markDynmapZone() {
		if (!active) return
		addLocus(this)
	}

	private fun removeDynmapZone() {
		removeLocus(this)
	}

	/** The location provider to give the spawner. */
	val spawnLocationProvider: Supplier<Location?> = Supplier {
		if (!active) return@Supplier null

		center.getLocationNear(0.0, radius)
	}

	private fun isOccupied(): Boolean {
		if (!active) return false
		val world = center.world
		val distSquared = radius * radius

		return ActiveStarships.getInWorld(world).any {
			val loc = it.centerOfMass.toVector().setY(LOCUS_Y)

			distanceSquared(loc, center.toVector()) < distSquared
		}
 	}

	companion object {
		const val LOCUS_Y = 192.0
		private val markerAPI: MarkerAPI get() = DynmapPlugin.plugin.markerAPI
		private val markerSet
			get() = markerAPI.getMarkerSet("events")
			?: markerAPI.createMarkerSet("events", "World Event Markers", null, false)

		fun addLocus(locus: LocusScheduler) {
			if (!dynmapLoaded) return

			val loc = locus.center

			markerSet.layerPriority = 10
			val marker = markerSet.createCircleMarker(
				"${locus.getSpawner().identifier}_LOCUS",
				locus.displayName.plainText(),
				true,
				loc.world.name,
				loc.x,
				loc.y,
				loc.z,
				locus.radius,
				locus.radius,
				false
			)

			marker.setFillStyle(0.2, locus.dynmapColor.value())
			marker.setLineStyle(5, 0.8, locus.dynmapColor.value())

			marker.description = """
				<p><h2>${locus.displayName.plainText()}</h2></p><p>
			""".trimIndent()
		}

		fun removeLocus(locus: LocusScheduler) {
			if (!dynmapLoaded) return
			markerSet.findCircleMarker("${locus.getSpawner().identifier}_LOCUS")?.deleteMarker()
		}
	}

	override fun getTickInfo(): String {
		return displayName.plainText()
	}
}
