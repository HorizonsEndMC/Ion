package net.horizonsend.ion.server.features.ai.configuration.steering

import kotlinx.serialization.Serializable

// In its own file
@Serializable
data class AIContextConfiguration(
	val defaultWanderContext: WanderContextConfiguration = WanderContextConfiguration(),

	val defaultCommitmentContext: CommitmentContextConfiguration = CommitmentContextConfiguration(),

	val defaultMomentumContextConfiguration: MomentumContextConfiguration = MomentumContextConfiguration(),

	val defaultOffsetSeekContextConfiguration: OffsetSeekContextConfiguration = OffsetSeekContextConfiguration(),
	val starfighterOffsetSeekContextConfiguration: OffsetSeekContextConfiguration =
		OffsetSeekContextConfiguration(maxHeightDiff = 10.0),
	val capitalOffsetSeekContextConfiguration: OffsetSeekContextConfiguration =
		OffsetSeekContextConfiguration(maxHeightDiff = 40.0),

	val defaultFaceSeekContextConfiguration: FaceSeekContextConfiguration = FaceSeekContextConfiguration(),
	val starfighterFaceSeekContextConfiguration: FaceSeekContextConfiguration =
		FaceSeekContextConfiguration(weight = 2.0, maxWeight = 4.0),
	val gunshipFaceSeekContextConfiguration: FaceSeekContextConfiguration =
		FaceSeekContextConfiguration(weight = 0.5, maxWeight = 1.0),

	val defaultGoalSeekContextConfiguration: GoalSeekContextConfiguration = GoalSeekContextConfiguration(),

	val defaultFleetGravityContextConfiguration: FleetGravityContextConfiguration = FleetGravityContextConfiguration(),

	val defaultShieldAwarenessContextConfiguration: ShieldAwarenessContextConfiguration = ShieldAwarenessContextConfiguration(),
	val gunshipShieldAwarenessContextConfiguration: ShieldAwarenessContextConfiguration = ShieldAwarenessContextConfiguration(weight = 1.5),
	val capitalShieldAwarenessContextConfiguration: ShieldAwarenessContextConfiguration = ShieldAwarenessContextConfiguration(weight = 3.0),

	val defaultShipDangerContextConfiguration: ShipDangerContextConfiguration = ShipDangerContextConfiguration(),

	val defaultBorderDangerContextConfiguration: BorderDangerContextConfiguration = BorderDangerContextConfiguration(),

	val defaultWorldBlockDangerContextConfiguration: WorldBlockDangerContextConfiguration = WorldBlockDangerContextConfiguration(),

	val defaultObstructionDangerContextConfiguration: ObstructionDangerContextConfiguration = ObstructionDangerContextConfiguration(),

) {
	@Serializable
	data class WanderContextConfiguration(
		val weight: Double = 0.5,
		val jitterRate: Double = 1000000.0,
		val sizeFactor: Double = 100.0,
	)

	@Serializable
	data class CommitmentContextConfiguration(
		val weight: Double = 0.0,
		val hist: Double = 0.95
	)

	@Serializable
	data class MomentumContextConfiguration(
		val weight: Double = 0.0,
		val falloff: Double = 1.0,
		val dotShift: Double = -0.2,
		val hist: Double = 0.8
	)

	@Serializable
	data class OffsetSeekContextConfiguration(
		val weight: Double = 1.0,
		val dotShift: Double = 0.0,
		val defaultOffsetDist : Double = 100.0,
		val maxHeightDiff : Double= 20.0
	)

	@Serializable
	data class FaceSeekContextConfiguration(
		val weight: Double = 0.0,
		val faceWeight: Double = 2.0,
		val maxWeight: Double = 0.0,
		val falloff: Double = 300.0
	)

	@Serializable
	data class GoalSeekContextConfiguration(
		val weight: Double = 1.0,
		val maxWeight: Double = 2.0,
		val falloff: Double = 1000.0
	)

	@Serializable
	data class FleetGravityContextConfiguration(
		val weight: Double = 1.0,
		val falloffMod: Double = 25.0
	)

	@Serializable
	data class ShieldAwarenessContextConfiguration(
		val weight: Double = 3.0,
		val criticalPoint: Double = 0.3,
		val power: Double = 1.5,
		val histDecay: Double = 0.98,
		val verticalDamp :Double = 0.3,
		val damageSensitivity : Double = 100.0,
	)

	@Serializable
	data class ShipDangerContextConfiguration(
		val falloff: Double = 50.0,
		val dotShift: Double = 0.2,
		val shipWeightSize: Double = 100.0,
		val shipWeightSpeed: Double = 20.0
	)

	@Serializable
	data class BorderDangerContextConfiguration(
		val falloff: Double = 10.0,
		val verticalFalloff: Double = 20.0,
		val dotShift: Double = 0.2
	)

	@Serializable
	data class WorldBlockDangerContextConfiguration(
		val falloff: Double = 2.0,
		val dotPower: Double = 3.0,
		val maxDist: Double = 200.0
	)

	@Serializable
	data class ObstructionDangerContextConfiguration(
		val falloff: Double = 4.0,
		val dotShift : Double = 0.3,
		val dotPower: Double = 1.0,
		val expireTime: Int = 5 * 1000,
	)
}
