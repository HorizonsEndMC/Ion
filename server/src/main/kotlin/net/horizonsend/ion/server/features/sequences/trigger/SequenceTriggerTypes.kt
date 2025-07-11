package net.horizonsend.ion.server.features.sequences.trigger

object SequenceTriggerTypes {
	private val types: MutableList<SequenceTriggerType<*>> = mutableListOf()

	val PLAYER_MOVEMENT = register(PlayerMovementTrigger)
	val PLAYER_INTERACT = register(PlayerInteractTrigger)
	val COMBINED_AND = register(CombinedAndTrigger)
	val COMBINED_OR = register(CombinedOrTrigger)
	val DATA_PREDICATE = register(DataPredicate)
	val USE_TRACTOR_BEAM = register(UsedTractorBeamTrigger)

	fun <T : SequenceTriggerType<*>> register(type: T): T {
		types.add(type)
		return type
	}

	fun runSetup() {
		types.forEach { t -> t.setupChecks() }
	}
}
