package net.horizonsend.ion.server.configuration.util

import kotlinx.serialization.Serializable
import java.time.Duration
import java.util.concurrent.TimeUnit

@Serializable
data class DurationConfig(
	val unit: TimeUnit,
	val length: Long,
) {
	fun toDuration(): Duration = Duration.of(length, unit.toChronoUnit())
}
