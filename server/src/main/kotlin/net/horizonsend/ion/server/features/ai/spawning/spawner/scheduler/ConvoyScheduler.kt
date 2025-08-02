package net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler

import kotlinx.serialization.Serializable
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_GRAY
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.ai.spawning.AISpawningManager
import net.horizonsend.ion.server.features.ai.spawning.spawner.AISpawner
import net.horizonsend.ion.server.features.ai.spawning.spawner.PersistentDataSpawnerComponent
import net.horizonsend.ion.server.features.ai.spawning.spawner.scheduler.ConvoyScheduler.ConvoyPersistentData
import net.horizonsend.ion.server.miscellaneous.utils.Notify
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Location
import org.slf4j.Logger
import java.time.Duration
import java.time.Instant
import java.time.ZoneOffset
import java.time.format.DateTimeFormatter
import java.util.function.Supplier
import kotlin.reflect.KClass

/**
 * Creates a locus spawner scheduler. The spawner will be executed at a higher rate for set period, near a specific location.
 **/
class ConvoyScheduler(
	override val storageKey: String,
	private val displayName: Component,
	private val separation: Supplier<Duration>,
	private val announcementMessage: Component?,
) : SpawnerScheduler, TickedScheduler, PersistentDataSpawnerComponent<ConvoyPersistentData>, StatusScheduler {
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

		if (announcementMessage != null) Notify.chatAndGlobal(
			template(
				announcementMessage,
				paramColor = HE_LIGHT_GRAY,
				useQuotesAroundObjects = false,
				center.world.name,
				center.blockX,
				center.blockY,
				center.blockZ
			)
		)
		getSpawner().trigger(logger, AISpawningManager.context)
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
			paramColor = HE_LIGHT_GRAY,
			useQuotesAroundObjects = false,
			displayName,
			UTC_TIME.format(nextStartInstant),// {1}
			String.format("%.1f", hoursLeft)// {2}
		)
	}

	override val typeClass: KClass<ConvoyPersistentData> = ConvoyPersistentData::class

	override fun load(data: ConvoyPersistentData) {
		lastActiveTime = data.lastActiveTime
		lastSeparation = Duration.ofMillis(data.lastSeparation)
	}

	override fun save(): ConvoyPersistentData? {
		return ConvoyPersistentData(lastActiveTime, lastSeparation.toMillis())
	}

	@Serializable
	data class ConvoyPersistentData(
		val lastActiveTime: Long,
		val lastSeparation: Long,
	)
}
