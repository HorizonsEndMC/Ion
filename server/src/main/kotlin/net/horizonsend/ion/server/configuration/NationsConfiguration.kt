package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.DurationConfig
import java.util.concurrent.TimeUnit

@Serializable
data class NationsConfiguration(
	val solarSiegeConfiguration: SolarSieges = SolarSieges()
) {
	@Serializable
	data class SolarSieges(
		val declareWindowStart: Long = 14L,
		val declareWindowDuration: DurationConfig = DurationConfig(TimeUnit.HOURS, 3),
		val preparationWindowDuration: DurationConfig = DurationConfig(TimeUnit.HOURS, 3),
		val activeWindowDuration: DurationConfig = DurationConfig(TimeUnit.MINUTES, 90),
		val participationLength: DurationConfig = DurationConfig(TimeUnit.MINUTES, 15),
		val playerKillPoints: Int = 1000,
		val shipCostMultiplier: Double = 1.0,
		val referenceDestroyerPrice: Int = 10_000,
	)
}
