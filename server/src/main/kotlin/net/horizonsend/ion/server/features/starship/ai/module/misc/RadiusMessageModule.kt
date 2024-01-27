package net.horizonsend.ion.server.features.starship.ai.module.misc

import net.horizonsend.ion.common.utils.text.colors.HEColorScheme
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.features.starship.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.utils.multimapOf
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import java.util.UUID

class RadiusMessageModule(
	controller: AIController,
	private val prefix: Component,
	private val messageDistanceMap: Map<Double, Component>
) : AIModule(controller) {
	// Map of players and already messaged distances
	private val alreadyMessaged = multimapOf<Double, UUID>()

	fun buildMessage(message: Component): Component = template(
		message = "{0}\n{1}: {2}",
		color = HEColorScheme.HE_MEDIUM_GRAY,
		paramColor = NamedTextColor.WHITE,
		useQuotesAroundObjects = true,
		prefix,
		starship.getDisplayName(),
		message
	)

	private fun checkMessage() {
		for ((distance, message) in messageDistanceMap) {
			val players = getCenter().toLocation(world).getNearbyPlayers(distance)
			val uuids = alreadyMessaged[distance]

			val built = buildMessage(message)

			for (player in players) {
				if (uuids.contains(player.uniqueId)) continue

				player.sendMessage(built)
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
