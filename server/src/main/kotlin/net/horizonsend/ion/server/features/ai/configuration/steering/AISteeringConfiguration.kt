package net.horizonsend.ion.server.features.ai.configuration.steering

import kotlinx.serialization.Serializable
@Serializable
data class AISteeringConfiguration(
	val defaultBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(),
	val starfighterBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 30.0, defaultRotationMixingRatio = 0.6),
	val gunshipBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 25.0, defaultRotationMixingRatio = 0.2),
	val corvetteBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 23.0, defaultRotationMixingRatio = 0.2),
	val miniFrigateBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 22.0, defaultRotationMixingRatio = 0.3),
	val frigateBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 20.0, defaultRotationMixingRatio = 0.1, defaultRotationContribution = 0.5),
	val destroyerBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 19.0, defaultRotationMixingRatio = 0.1, defaultRotationContribution = 0.5),
	val battlecruiserBasicSteeringConfiguration : BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 15.0, defaultRotationMixingRatio = 0.1, defaultRotationContribution = 0.0),

	val starfighterDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 600.0, optimalDist = 100.0),
	val gunshipDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 600.0, optimalDist = 130.0),
	val interdictionCorvetteDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 2300.0, optimalDist = 1500.0),
	val logisticCorvetteDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 2300.0, optimalDist = 130.0, startFleeing = 0.1, stopFleeing = 0.3),
	val miniFrigateDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 300.0, optimalDist = 100.0),
	val capitalDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 600.0, optimalDist = 150.0, startFleeing = 0.1, stopFleeing = 0.5),//capitals back off
	val advancedCapitalDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 600.0, optimalDist = 100.0, startFleeing = 0.01, stopFleeing = 0.3),
	val battlecruiserDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(maxDist = 600.0, optimalDist = 200.0, startFleeing = -0.1, stopFleeing = 0.0) //BCs dont flee
) {
	@Serializable
	data class BasicSteeringConfiguration(
		val defaultMaxSpeed : Double = 20.0,
		val defaultRotationContribution : Double = 0.2,
		val defaultRotationMixingRatio : Double = 0.0,
		val defaultRotationMixingPower : Double = 0.5
	)

	@Serializable
	data class DistanceConfiguration(
		val minDist: Double = 0.0,
		val maxDist: Double = 1000.0, //set this to just beyond the standoff position
		val optimalDist: Double = 300.0,
		val startFleeing: Double = 0.2,
		val stopFleeing: Double = 0.8
	)

}
