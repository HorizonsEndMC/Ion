package net.horizonsend.ion.server.features.sequences.phases

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys

object SequencePhaseKeys : KeyRegistry<SequencePhase>(RegistryKeys.SEQUENCE_PHASE, SequencePhase::class) {
	val TUTORIAL_START = registerKey("REAL_TUTORIAL_START")
	val EXIT_CRYOPOD_ROOM = registerKey("EXIT_CRYOPOD_ROOM")
	val BRANCH_LOOK_OUTSIDE = registerKey("LOOK_OUTSIDE")
	val BROKEN_ELEVATOR = registerKey("BROKEN_ELEVATOR")
	val LOOK_AT_TRACTOR = registerKey("LOOK_AT_TRACTOR")
	val CREW_QUARTERS = registerKey("CREW_QUARTERS")
	val BRANCH_DYNMAP = registerKey("BRANCH_DYNMAP")
	val BRANCH_SHIP_COMPUTER = registerKey("BRANCH_SHIP_COMPUTER")
	val FIRE_OBSTACLE = registerKey("FIRE_OBSTACLE")
	val BRANCH_NAVIGATION = registerKey("BRANCH_NAVIGATION")
	val BRANCH_MULTIBLOCKS = registerKey("BRANCH_MULTIBLOCKS")
	val GET_CHETHERITE = registerKey("GET_CHETHERITE")
	val RECEIVED_CHETHERITE = registerKey("RECEIVED_CHETHERITE")
	val ENTERED_ESCAPE_POD = registerKey("ENTERED_ESCAPE_POD")
}
