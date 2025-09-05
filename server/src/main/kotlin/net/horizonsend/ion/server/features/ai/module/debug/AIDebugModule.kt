package net.horizonsend.ion.server.features.ai.module.debug


import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.steering.BasicSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.context.ContextMap
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class AIDebugModule(controller: AIController) : AIModule(controller) {
	companion object {
		val contextMapTypes = setOf(
			"movementInterest",
			"rotationInterest",
			"danger",
			"wander",
			"offsetSeek",
			"faceSeek",
			"fleetGravity",
			"shieldAwareness",
			"shipDanger",
			"borderDanger",
			"worldBlockDanger",
			"obstructionDanger",
			"incomingFire",
			"commitment"
		)

		val shownContexts = mutableListOf(
			Pair("danger", DebugColor.RED),
			Pair("movementInterest", DebugColor.GREEN)
		)

		private val debugOffsetIncrement = 0.6
		private val debugOffset = 2.0

		enum class DebugColor { WHITE, RED, BLUE, GREEN }

		var visualDebug = false
		var canShipsMove = true
		var canShipsRotate = true
		var showAims = false
		var fireWeapons = true
	}

	// These vars are for saving the info of the closest
	private var displayVectors = mutableListOf<VectorDisplay>()
	private var rendered = false

	override fun tick() {
		renderDebug()
	}

	/**
	 * Creates a client-side ItemDisplay entity for rendering planet icons in space.
	 * @return the NMS ItemDisplay object
	 * @param player the player that the entity should be visible to
	 * @param identifier the string used to retrieve the entity later
	 * @param distance the distance that the planet is to the player
	 * @param direction the direction that the entity will render from with respect to the player
	 */
	private fun createAIShipDebug(): MutableList<VectorDisplay> {
		val mod = controller.getCoreModuleByType<BasicSteeringModule>() ?: return mutableListOf()
		val output = mutableListOf<VectorDisplay>()
		val shipOffset = (controller.starship.max - controller.starship.centerOfMass).y.toDouble()
		for (i in 0 until shownContexts.size) {
			val offset = i * debugOffsetIncrement + debugOffset + shipOffset
			output.addAll(
				displayContext(
					mod.contexts[shownContexts[i].first]!!,
					mapColor(shownContexts[i].second), controller.starship, Vector(0.0, offset, 0.0)
				)
			)
		}
		val dirOffset = shownContexts.size * debugOffsetIncrement + debugOffset + shipOffset
		output.add(VectorDisplay(mod::thrustOut, mapColor(DebugColor.BLUE), controller.starship, Vector(0.0, dirOffset, 0.0)))
		output.add(VectorDisplay(mod::headingOut, mapColor(DebugColor.RED), controller.starship, Vector(0.0, dirOffset, 0.0)))

		val powerModeOffset = dirOffset + 0.5
		output.addAll(addPowerModeDisplay(Vector(0.0, powerModeOffset, 0.0)))
		return output
	}

	private fun displayContext(
		context: ContextMap,
		model: ItemStack,
		identifier: ActiveStarship, offset: Vector
	): List<VectorDisplay> {

		return (0 until ContextMap.NUMBINS).map { i ->
			ContextMap.bindir[i]
			val display = VectorDisplay(context, i, model, identifier, offset)
			display
		}
	}

	private fun addPowerModeDisplay(baseOffset: Vector): List<VectorDisplay> {
		val shieldOffset = baseOffset.clone().add(Vector(-0.2, 0.0, 0.0))
		val weaponsOffset = baseOffset.clone()
		val thrustOffset = baseOffset.clone().add(Vector(0.2, 0.0, 0.0))
		val powerDist = controller.starship.reactor.powerDistributor

		val up = Vector(0.0, 1.0, 0.0)
		return listOf(
			VectorDisplay(powerDist::shieldPortion, up, mapColor(DebugColor.BLUE), controller.starship, shieldOffset),
			VectorDisplay(powerDist::weaponPortion, up, mapColor(DebugColor.RED), controller.starship, weaponsOffset),
			VectorDisplay(powerDist::thrusterPortion, up, mapColor(DebugColor.GREEN), controller.starship, thrustOffset)
		)
	}

	private fun renderDebug() {
		if (!rendered) {
			displayVectors = createAIShipDebug()
			rendered = true
		}
		displayVectors.forEach { it.update() }
		val mod = controller.getCoreModuleByType<BasicSteeringModule>() ?: return
		val particle = Particle.DUST
		val dustOptions = Particle.DustOptions(Color.BLUE, 4f)
		val orbitTarget = mod.orbitTarget
		if (orbitTarget != null) {
			controller.starship.world.spawnParticle(particle, orbitTarget.x, orbitTarget.y, orbitTarget.z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions, true)
		}
	}

	private fun mapColor(color: DebugColor): ItemStack {
		return when (color) {
			DebugColor.RED -> CustomItemKeys.DEBUG_LINE_RED.getValue().constructItemStack()
			DebugColor.GREEN -> CustomItemKeys.DEBUG_LINE_GREEN.getValue().constructItemStack()
			DebugColor.BLUE -> CustomItemKeys.DEBUG_LINE_BLUE.getValue().constructItemStack()
			DebugColor.WHITE -> CustomItemKeys.DEBUG_LINE.getValue().constructItemStack()
		}
	}
}
