package net.horizonsend.ion.server.features.explosion

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.Block

/**
 * Mostly async starship explosion, supporting the controller API
 * @see Controller
 **/
class StarshipExplosion(
	val world: World,
	val x: Double,
	val y: Double,
	val z: Double,
	val power: Float,
	val originator: Controller,
	val blocks: MutableList<Block> = mutableListOf()
) {
	var useRays = false
	var useFire = false

	fun explode(applyPhysics: Boolean = true, callback: () -> Unit = {}) {
		getBlocksAsync()

		val event = StarshipCauseExplosionEvent(
			originator,
			this
		)

		val isCancelled = event.callEvent()

		if (isCancelled) return

		removeBlocksAsync(applyPhysics)

		Tasks.sync(callback)
	}

	/** populates the blocks list **/
	fun getBlocksAsync() {
		//TODO
	}

	/** removes blocks specified in the blocks list **/
	fun removeBlocksAsync(applyPhysics: Boolean) {
		for (block in blocks) {
			block.setType(Material.AIR, applyPhysics)
		}
	}
}
