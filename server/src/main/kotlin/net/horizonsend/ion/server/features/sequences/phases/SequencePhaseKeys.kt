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
	val GO_TO_ESCAPE_POD = registerKey("GO_TO_ESCAPE_POD")
	val BRANCH_CARGO_CRATES = registerKey("BRANCH_CARGO_CRATES")
	val ENTERED_ESCAPE_POD = registerKey("ENTERED_ESCAPE_POD")
	val FLIGHT_SHIFT = registerKey("SHIFT_FLIGHT")
	val FLIGHT_ROTATION = registerKey("FLIGHT_ROTATION")
	val FLIGHT_INTERMISSION = registerKey("FLIGHT_INTERMISSION")
	val FLIGHT_CRUISE = registerKey("FLIGHT_CRUISE")
	val FLIGHT_CRUISE_TURN = registerKey("FLIGHT_CRUISE_TURN")
	val FLIGHT_CRUISE_STOP = registerKey("FLIGHT_CRUISE_STOP")
	val FLIGHT_HYPERSPACE_JUMP = registerKey("FLIGHT_HYPERSPACE_JUMP")
	val FLIGHT_EXIT_HYPERSPACE = registerKey("FLIGHT_EXIT_HYPERSPACE")
	val FLIGHT_PARKING = registerKey("FLIGHT_PARKING")
	val FLIGHT_LEAVE_POD = registerKey("FLIGHT_LEAVE_POD")
//	val TRANSIT_HUB = registerKey("SHIFT_FLIGHT")
//	val TRANSIT_HUB = registerKey("SHIFT_FLIGHT")
}
