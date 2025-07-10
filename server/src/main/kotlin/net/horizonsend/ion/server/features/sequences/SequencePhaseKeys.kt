package net.horizonsend.ion.server.features.sequences

object SequencePhaseKeys {
	val keys: MutableList<SequencePhaseKey> = mutableListOf()
	val byString: MutableMap<String, SequencePhaseKey> = mutableMapOf()

	class SequencePhaseKey(val key: String) {
		fun getValue(): SequencePhase {
			return SequenceManager.getPhaseByKey(this)
		}
	}

	val TUTORIAL_START = register("TUTORIAL_START")
	val TUTORIAL_TWO = register("TUTORIAL_TWO")
	val TUTORIAL_BRANCH = register("TUTORIAL_BRANCH")
	val TUTORIAL_END = register("TUTORIAL_END")

	fun register(key: String): SequencePhaseKey {
		val phaseKey = SequencePhaseKey(key)
		keys.add(phaseKey)
		byString[key] = phaseKey
		return phaseKey
	}
}
