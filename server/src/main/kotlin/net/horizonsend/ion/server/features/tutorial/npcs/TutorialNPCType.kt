package net.horizonsend.ion.server.features.tutorial.npcs

import net.citizensnpcs.api.event.NPCRightClickEvent
import net.horizonsend.ion.common.utils.text.colors.EXPLORER_MEDIUM_CYAN
import net.horizonsend.ion.common.utils.text.colors.MINING_CORP_LIGHT_ORANGE
import net.horizonsend.ion.common.utils.text.colors.PIRATE_DARK_RED
import net.horizonsend.ion.common.utils.text.miniMessage
import net.horizonsend.ion.server.features.tutorial.tutorials.FlightTutorial
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text

enum class TutorialNPCType(val displayName: Component, val billboardText: List<Component> = listOf()) {
	FLIGHT_TUTORIAL(text("TUTORIAL NPC", PIRATE_DARK_RED), listOf(text("1", MINING_CORP_LIGHT_ORANGE), text("2", EXPLORER_MEDIUM_CYAN), text("3", PIRATE_DARK_RED))) {
		override fun onRightClick(event: NPCRightClickEvent) {
			FlightTutorial.startTutorial(event.clicker)
		}
	},

	MINIMESSAGE_TEST("<rainbow>|||||||||||||||||||||||||||||".miniMessage()) {
		override fun onRightClick(event: NPCRightClickEvent) {
		}
	}

	;

	open fun onRightClick(event: NPCRightClickEvent) {}
}
