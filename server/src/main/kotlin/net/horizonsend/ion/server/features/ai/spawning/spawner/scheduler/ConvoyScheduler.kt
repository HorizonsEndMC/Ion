package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import net.horizonsend.ion.common.database.cache.AIEncounterCache
import net.horizonsend.ion.common.database.schema.misc.AIEncounterData
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.nations.NationsMap.dynmapLoaded
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.player.PlayerController
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.dynmap.bukkit.DynmapPlugin
import org.dynmap.markers.MarkerAPI
import org.slf4j.Logger
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Supplier

/**
 * Creates a locus spawner scheduler. The spawner will be executed at a higher rate for set period, near a specific location.
 **/
class ConvoyScheduler(
	private val displayName: Component,
	private val separation: Supplier<Duration>,
	private val announcementMessage: Component?,
) : SpawnerScheduler, TickedScheduler, PersistentScheduler, StatusScheduler{
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
	var difficulty: Int = 2

	private var lastActiveTime = System.currentTimeMillis()
	/** How long to wait after the previous locus ended before we may start the next one */
	private var lastSeparation: Duration = separation.get()   // first run

	override fun tick(logger: Logger) {
		// Interval from the end of the last one
		val interval = System.currentTimeMillis() - lastActiveTime
		// Start the locus if the separation has passed
		if (interval > lastSeparation.toMillis()) start(logger)
	}

	fun start(logger: Logger) {
		lastActiveTime = System.currentTimeMillis()
		lastSeparation = separation.get()

		if (announcementMessage != null) Notify.chatAndGlobal(template(
			announcementMessage,
			paramColor = HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			center.world.name,
			center.blockX,
			center.blockY,
			center.blockZ
		))
		getSpawner().trigger(logger, AISpawningManager.context)
	}

	override fun loadData() {
		val data = AIEncounterCache[spawner.identifier]
		if (data != null) {
			lastActiveTime = data.lastActiveTime
			lastSeparation = Duration.ofMillis(data.lastSeparation)
		}
	}

	override fun saveData() {
		val data = AIEncounterCache[spawner.identifier]
		if (data == null) {
			AIEncounterData.create(spawner.identifier, lastActiveTime, 0L, lastSeparation.toMillis())
			return
		}
		AIEncounterData.saveData(data._id, lastActiveTime, 0L, lastSeparation.toMillis())
	}


	override fun getTickInfo(): String {
		return displayName.plainText()
	}

	private val UTC_TIME: DateTimeFormatter =
		DateTimeFormatter.ofPattern("d M HH:mm 'UTC'").withZone(ZoneOffset.UTC)

	override fun getStatus(): Component {
		val now = Instant.now()

		// ───── compute next start ─────
		val nextStartInstant = Instant.ofEpochMilli(lastActiveTime).plusMillis(lastSeparation.toMillis())

		val hoursLeft = (Duration.between(now, nextStartInstant).toMinutes().toDouble() / 60)

		return template(
			message = text("{0} starts at: {1} ({2} hours from now)", HEColorScheme.HE_MEDIUM_GRAY),
			paramColor = HEColorScheme.HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			displayName,
			UTC_TIME.format(nextStartInstant),// {1}
			String.format("%.1f", hoursLeft)// {2}
		)
	}
}
