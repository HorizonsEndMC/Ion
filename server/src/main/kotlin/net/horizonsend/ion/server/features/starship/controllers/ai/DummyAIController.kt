package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.movement.AIControlUtils
import net.horizonsend.ion.server.miscellaneous.utils.keysSortedByValue
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.kyori.adventure.text.format.TextDecoration
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.block.BlockState
import java.util.UUID

class DummyAIController(starship: ActiveStarship, uuid: UUID) : AIController(starship, "AI", uuid) {
	override val pilotName: Component = text("AI Pilot Matrix", NamedTextColor.RED, TextDecoration.BOLD)

	override var yaw: Float = 0.0F
	override var pitch: Float = 0.0F

	override var selectedDirectControlSpeed: Int = 0

	override var isShiftFlying: Boolean = false

	override fun canDestroyBlock(block: Block): Boolean {
		return false
	}

	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean {
		return false
	}

	override fun tick() {
		val location = starship.centerOfMass.toLocation(starship.world)
		val nearestPlayer = getNearestPlayer(location)

		val direction = nearestPlayer?.location?.toVector()?.subtract(starship.centerOfMass.toVector())

		direction?.let { AIControlUtils.faceDirection(this, vectorToBlockFace(direction)) }

		AIControlUtils.shiftFlyTowardsPlayer(this, nearestPlayer)

		nearestPlayer?.let {
			AIControlUtils.shootAtPlayer(this, nearestPlayer, true)
			AIControlUtils.shootAtPlayer(this, nearestPlayer, false, weaponSet = "phasers")
		}
	}

	private fun getNearestPlayer(location: Location) = IonServer.server.onlinePlayers
		.filter { it.world == starship.world }
		.associateWith { it.location.distance(location) }
		.filter { it.value <= 5000 }
		.filter { it.value >= 50 }
		.keysSortedByValue()
		.firstOrNull()
}
