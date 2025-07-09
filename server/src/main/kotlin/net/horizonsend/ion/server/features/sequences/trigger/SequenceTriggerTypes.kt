package net.horizonsend.ion.server.features.sequences.trigger

object SequenceTriggerTypes {
	private val types: MutableList<SequenceTriggerType<*>> = mutableListOf()

	val PLAYER_MOVEMENT = register(SequenceTriggerType.PlayerMovementTrigger)
	val PLAYER_INTERACT = register(SequenceTriggerType.PlayerInteractTrigger)

	fun <T : SequenceTriggerType<*>> register(type: T): T {
		types.add(type)
		return type
	}

	fun runSetup() {
		types.forEach { t -> t.setup() }
	}
}
