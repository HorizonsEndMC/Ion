package net.horizonsend.ion.server.features.sequences

object SequencePhaseKeys {
	val keys: MutableList<SequencePhaseKey> = mutableListOf()
	val byString: MutableMap<String, SequencePhaseKey> = mutableMapOf()

	class SequencePhaseKey(val key: String) {
		fun getValue(): SequencePhase {
			return SequenceManager.getPhaseByKey(this)
		}
	}

	val REAL_TUTORIAL_START = register("REAL_TUTORIAL_START")
	val EXIT_CRYOPOD_ROOM = register("EXIT_CRYOPOD_ROOM")
	val BRANCH_LOOK_OUTSIDE = register("LOOK_OUTSIDE")
	val BROKEN_ELEVATOR = register("BROKEN_ELEVATOR")
	val LOOK_AT_TRACTOR = register("LOOK_AT_TRACTOR")
	val USED_TRACTOR_BEAM = register("USED_TRACTOR_BEAM")

	val TUTORIAL_START = register("TUTORIAL_START")
	val TUTORIAL_TWO = register("TUTORIAL_TWO")
	val CHERRY_TEST_BRANCH = register("TUTORIAL_BRANCH")
	val TUTORIAL_END = register("TUTORIAL_END")

	fun register(key: String): SequencePhaseKey {
		val phaseKey = SequencePhaseKey(key)
		keys.add(phaseKey)
		byString[key] = phaseKey
		return phaseKey
	}
}
