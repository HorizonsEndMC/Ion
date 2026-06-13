package net.horizonsend.ion.server.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.configuration.util.DurationConfig
import java.util.concurrent.TimeUnit

@Serializable
data class NationsConfiguration(
	val solarSiegeConfiguration: SolarSieges = SolarSieges(),
	val dominionTerritorySiegeConfiguration: DominionTerritorySieges = DominionTerritorySieges()
) {
	@Serializable
	data class SolarSieges(
		val declareWindowStart: Long = 14L,
		val declareWindowDuration: DurationConfig = DurationConfig(TimeUnit.HOURS, 3),
		val preparationWindowDuration: DurationConfig = DurationConfig(TimeUnit.HOURS, 3),
		val activeWindowDuration: DurationConfig = DurationConfig(TimeUnit.MINUTES, 90),
		val participationLength: DurationConfig = DurationConfig(TimeUnit.MINUTES, 15),
		val playerKillPoints: Int = 5_000,
		val shipCostMultiplier: Double = 1.0,
		val referenceDestroyerPrice: Int = 19_765,
		val ignoreSiegeWindow: Boolean = false,
		val rewardPointCap: Int = 100_000,
		val minimumPassivePointsShipSize: Int = 350
	)

	@Serializable
	data class DominionTerritorySieges(
		val preparationWindowDuration: DurationConfig = DurationConfig(TimeUnit.MINUTES, 60),
		val activeWindowDuration: DurationConfig = DurationConfig(TimeUnit.MINUTES, 60),
		val participationLength: DurationConfig = DurationConfig(TimeUnit.MINUTES, 3),
		val playerKillPoints: Int = 1,
		val passivePoints: Double = 0.25,
		val subCapitalKillPoints: Double = 300.0,
		val capitalKillPoints: Double = 500.0,
		val superCapitalKillPoints: Double = 1000.0,
		val miningShipKillPoints: Double = 1.0,
		val tech2Multiplier: Double = 2.0,
		val shipCostMultiplier: Double = 1.0,
		val minimumPassivePointsShipSize: Int = 350
	)
}
