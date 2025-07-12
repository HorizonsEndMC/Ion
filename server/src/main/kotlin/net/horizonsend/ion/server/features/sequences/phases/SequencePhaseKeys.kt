package net.horizonsend.ion.server.features.sequences.phases

object SequencePhaseKeys {
	val keys: MutableList<SequencePhaseKey> = mutableListOf()
	val byString: MutableMap<String, SequencePhaseKey> = mutableMapOf()

	class SequencePhaseKey(val key: String) {
		fun getValue(): SequencePhase {
			return SequencePhases.getPhaseByKey(this)
		}
	}

	val TUTORIAL_START = register("REAL_TUTORIAL_START")
	val EXIT_CRYOPOD_ROOM = register("EXIT_CRYOPOD_ROOM")
	val BRANCH_LOOK_OUTSIDE = register("LOOK_OUTSIDE")
	val BROKEN_ELEVATOR = register("BROKEN_ELEVATOR")
	val LOOK_AT_TRACTOR = register("LOOK_AT_TRACTOR")
	val CREW_QUARTERS = register("CREW_QUARTERS")
	val BRANCH_DYNMAP = register("BRANCH_DYNMAP")
	val BRANCH_SHIP_COMPUTER = register("BRANCH_SHIP_COMPUTER")
	val FIRE_OBSTACLE = register("FIRE_OBSTACLE")
	val GET_CHETHERITE = register("FIRE_OBSTACLE")
	val RECEIVED_CHETHERITE = register("FIRE_OBSTACLE")

	fun register(key: String): SequencePhaseKey {
		val phaseKey = SequencePhaseKey(key)
		keys.add(phaseKey)
		byString[key] = phaseKey
		return phaseKey
	}
}
