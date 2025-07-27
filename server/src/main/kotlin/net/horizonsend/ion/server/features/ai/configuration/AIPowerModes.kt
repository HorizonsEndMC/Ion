package net.horizonsend.ion.server.features.ai.configuration

import kotlinx.serialization.Serializable
import net.horizonsend.ion.server.features.ai.module.misc.PowerModeModule


@Serializable
data class AIPowerModes(
	val defaultAIPowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration(),

	val starfighterPowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration(),

	val gunshipPowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration(),

	val corvettePowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration(),

	val capitalPowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration(),

	val superCapitalPowerModeConfiguration: AIPowerModeConfiguration = AIPowerModeConfiguration()
) {
	@Serializable
	data class AIPowerModeConfiguration(
		val baseShieldScore: Double = 1.0,
		val criticalShieldMultiplier: Double = 1.0,
		val shieldDistanceMultiplier: Double = 2.0,

		val baseWeaponsScore: Double = 2.5,
		val weaponsDistanceMultiplier: Double = 0.5,

		val baseThrustScore: Double = 0.5,
		val thrustSpeedMultiplier: Double = 2.5,
		val thrustSpeedPower: Double = 1.5,
		val thrustDirectionMultiplier: Double = 2.0,
		val thrustDriftMultiplier: Double = 3.0,

		val powermodes: List<PowerModeModule.PowerMode> = listOf(
			PowerModeModule.PowerMode(0.4, 0.5, 0.1, false, false),
			PowerModeModule.PowerMode(0.4, 0.1, 0.5, false, false),
			PowerModeModule.PowerMode(0.1, 0.5, 0.4, false, false),
			PowerModeModule.PowerMode(0.4, 0.4, 0.2, false, true),
		)
	)
}
