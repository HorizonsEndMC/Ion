package net.horizonsend.ion.server.features.starship.controllers.ai

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import org.bukkit.block.Block
import org.bukkit.block.BlockState

abstract class AIController(
	starship: ActiveStarship,
	name: String,
	private val criteria: Array<out Criterias.Criteria>
) : Controller(starship, name) {
	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 0

	var lastRotation: Long = 0L

	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	override fun tick() {
		val decision = criteria.associateWith { it.decision(this) }

		decision.filter { it.value }.keys.forEach { it.action(this) }

		super.tick()
	}
}
