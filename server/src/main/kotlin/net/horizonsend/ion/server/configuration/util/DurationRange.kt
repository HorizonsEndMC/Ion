package net.horizonsend.ion.server.configuration.util

import kotlinx.serialization.Serializable
import java.time.Duration
import kotlin.random.Random

@Serializable
data class DurationRange(
	val minimum: DurationConfig,
	val maximum: DurationConfig,
) {
	fun getRandomInRange(): Duration {
		return Duration.ofMillis(minimum.toDuration().toMillis() + Random.nextLong(maximum.toDuration().toMillis() - minimum.toDuration().toMillis()))
	}
}
