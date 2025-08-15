package net.horizonsend.ion.server.features.ai.configuration.steering

import kotlinx.serialization.Serializable

@Serializable
data class AISteeringConfiguration(
	val defaultBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(),
	val starfighterBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 30.0, defaultRotationMixingBias = 0.6, defaultRotationMixingGain = 6.0),
	val gunshipBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 25.0, defaultRotationMixingBias = 0.5, defaultRotationMixingGain = 2.0),
	val corvetteBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 23.0, defaultRotationMixingBias = 0.3, defaultRotationMixingGain = 1.0),
	val miniFrigateBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 22.0, defaultRotationMixingBias = 0.6, defaultRotationMixingGain = 2.0),
	val frigateBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 20.0, defaultRotationContribution = 0.3, defaultRotationBleed = 0.05, defaultRotationMixingBias = 0.3),
	val destroyerBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 19.0, defaultRotationMixingBias = 0.1, defaultRotationContribution = 0.5),
	val battlecruiserBasicSteeringConfiguration: BasicSteeringConfiguration =
		BasicSteeringConfiguration(defaultMaxSpeed = 15.0, defaultRotationMixingBias = 0.1, defaultRotationContribution = 0.5),

	val starfighterDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0, minDist = 30.0, optimalDist = 100.0, maxDist = 180.0
	),
	val gunshipDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0, optimalDist = 150.0, maxDist = 220.0
	),
	val corvetteDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 350.0, optimalDist = 150.0, maxDist = 500.0
	),
	val interdictionCorvetteDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 500.0, optimalDist = 1500.0, maxDist = 2000.0
	),
	val logisticCorvetteDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 500.0, optimalDist = 150.0, maxDist = 500.0, startFleeing = 0.1, stopFleeing = 0.3
	),
	val miniFrigateDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0, optimalDist = 140.0, maxDist = 500.0
	),
	//because cruising cases the ships to go wide of the optima dist make it closer than normal
	val capitalDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0, minDist = 50.0,maxDist = 300.0, optimalDist = 150.0, startFleeing = 0.01, stopFleeing = 0.2, fleeChance = 0.002,
	),//capitals back off
	val advancedCapitalDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0,maxDist = 300.0, optimalDist = 50.0, startFleeing = 0.01, stopFleeing = 0.3,  fleeChance = 0.002,
	),
	val battlecruiserDistanceConfiguration: DistanceConfiguration = DistanceConfiguration(
		fleeDist = 300.0, optimalDist = 200.0, startFleeing = -0.1, stopFleeing = 0.0
	) //BCs dont flee
) {
	@Serializable
	data class BasicSteeringConfiguration(
		val defaultMaxSpeed: Double = 20.0,
		val defaultRotationContribution: Double = 0.2,
		val defaultRotationBleed: Double = 0.1,
		val defaultRotationMixingBias: Double = 0.5,
		val defaultRotationMixingGain: Double = 1.0
	)

	@Serializable
	data class DistanceConfiguration(
		val minDist: Double = 0.01,
		val maxDist: Double = 500.0,
		val fleeDist: Double = 1000.0, //set this to just beyond the standoff position
		val optimalDist: Double = 300.0,
		val startFleeing: Double = 0.3,
		val stopFleeing: Double = 0.8,
		val agressionTolerance: Double = 0.5,
		val fleeChance: Double = 0.005 // evaluated every tick
	)

}
