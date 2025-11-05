package net.horizonsend.ion.server.features.sequences.trigger

object SequenceTriggerTypes {
	private val types: MutableList<SequenceTriggerType<*>> = mutableListOf()

	val PLAYER_MOVEMENT = register(PlayerMovementTrigger)
	val PLAYER_INTERACT = register(PlayerInteractTrigger)
	val COMBINED_AND = register(CombinedAndTrigger)
	val COMBINED_OR = register(CombinedOrTrigger)
	val DATA_PREDICATE = register(DataPredicate)
	val USE_TRACTOR_BEAM = register(UsedTractorBeamTrigger)
	val CONTAINS_ITEM = register(ContainsItemTrigger)
	val STARSHIP_CRUISE = register(StarshipCruiseTrigger)
	val STARSHIP_MANUAL_FLIGHT = register(ShipManualFlightTrigger)
	val STARSHIP_ROTATE = register(ShipRotateTrigger)
	val STARSHIP_ENTER_HYPERSPACE = register(ShipEnterHyperspaceJumpTrigger)
	val WAIT_TIME = register(WaitTimeTrigger)

	fun <T : SequenceTriggerType<*>> register(type: T): T {
		types.add(type)
		return type
	}

	fun runSetup() {
		types.forEach { t -> t.setupChecks() }
	}
}
