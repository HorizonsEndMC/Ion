package net.horizonsend.ion.server.features.tutorial.npcs

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.starship.ai.spawning.pirate.PIRATE_DARK_RED
import net.horizonsend.ion.server.features.tutorial.FlightTutorialPhase
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

enum class TutorialNPCType(
	val npcName: Component,
	val underName: Component? = null
) {
	FLIGHT_TUTORIAL(text("TUTORIAL NPC", PIRATE_DARK_RED)) {
		override fun onRightClick(event: NPCRightClickEvent) {
			FlightTutorialPhase.start(event.clicker)
		}
	},

	MINIMESSAGE_TEST("<rainbow>||||||||||||".miniMessage()) {
		override fun onRightClick(event: NPCRightClickEvent) {
			println("received right click")
		}
	}

	;

	open fun onRightClick(event: NPCRightClickEvent) {}
}
