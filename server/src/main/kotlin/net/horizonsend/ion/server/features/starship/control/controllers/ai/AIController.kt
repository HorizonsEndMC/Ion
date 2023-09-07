package net.horizonsend.ion.server.features.starship.control.controllers.ai

import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.Controller
import net.kyori.adventure.text.Component
import org.bukkit.block.Block
import org.bukkit.block.BlockState

abstract class AIController(
	starship: ActiveStarship,
	name: String
) : Controller(starship, name) {
	override var isShiftFlying: Boolean = false
	override var pitch: Float = 0f
	override var yaw: Float = 0f
	override var selectedDirectControlSpeed: Int = 0

	var lastRotation: Long = 0L

	override fun canDestroyBlock(block: Block): Boolean = false
	override fun canPlaceBlock(block: Block, newState: BlockState, placedAgainst: Block): Boolean = false

	override fun getDisplayName(): Component = pilotName

	override fun rewardXP(xp: Int) {}
	override fun rewardMoney(credits: Double) {}
}
