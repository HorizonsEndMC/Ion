package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.text.Component
import java.util.UUID

class RadiusMessageModule(
	controller: AIController,
	private val messageDistanceMap: Map<Double, Component>
) : AIModule(controller) {
	// Map of players and already messaged distances
	private val alreadyMessaged = multimapOf<Double, UUID>()

	private fun checkMessage() {
		for ((distance, message) in messageDistanceMap) {
			val players = world.getNearbyPlayers(getCenter(), distance)
			val uuids = alreadyMessaged[distance]

			for (player in players) {
				if (uuids.contains(player.uniqueId)) continue

				player.sendMessage(message)
				uuids.add(player.uniqueId)
			}
		}
	}

	private var ticks = 0

	override fun tick() {
		ticks++
		if ((ticks % 50) != 0) return

		checkMessage()
	}
}
