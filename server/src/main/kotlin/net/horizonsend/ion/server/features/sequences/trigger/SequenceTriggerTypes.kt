package net.horizonsend.ion.server.features.sequences.trigger

object SequenceTriggerTypes {
	private val types: MutableList<SequenceTriggerType<*>> = mutableListOf()

	val PLAYER_MOVEMENT = register(PlayerMovementTrigger)
	val PLAYER_INTERACT = register(PlayerInteractTrigger)
	val PLAYER_DROP_ITEM = register(PlayerDropItemTrigger)
	val PLAYER_CHANGED_WORLD = register(PlayerChangedWorldTrigger)
	val COMBINED_AND = register(CombinedAndTrigger)
	val COMBINED_OR = register(CombinedOrTrigger)
	val DATA_PREDICATE = register(DataPredicate)
	val USE_TRACTOR_BEAM = register(UsedTractorBeamTrigger)
	val HAS_ITEM_IN_INVENTORY = register(HasItemInInventoryTrigger)
	val HAS_ITEM_EQUIPPED = register(HasItemEquippedTrigger)
	val STARSHIP_MOVEMENT = register(StarshipMovementTrigger)
	val STARSHIP_CRUISE_START = register(StarshipCruiseStartTrigger)
	val STARSHIP_CRUISE_STOP = register(StarshipCruiseStopTrigger)
	val STARSHIP_MANUAL_FLIGHT = register(ShipManualFlightTrigger)
	val STARSHIP_ROTATE = register(ShipRotateTrigger)
	val STARSHIP_ENTER_HYPERSPACE = register(ShipEnterHyperspaceJumpTrigger)
	val WAIT_TIME = register(WaitTimeTrigger)
	val PRE_EXIT_HYPERSPACE = register(ShipPreExitHyperspaceJumpTrigger)
	val STARSHIP_RELEASE = register(StarshipReleaseTrigger)
	val HYPERDRIVE_HAS_FUEL = register(HyperdriveHasFuelTrigger)
	val STARSHIP_JUMP_WARMUP = register(StarshipJumpWarmupTrigger)
	val STARSHIP_UNPILOT = register(StarshipUnpilotTrigger)

	fun <T : SequenceTriggerType<*>> register(type: T): T {
		types.add(type)
		return type
	}

	fun runSetup() {
		types.forEach { t -> t.setupChecks() }
	}
}
