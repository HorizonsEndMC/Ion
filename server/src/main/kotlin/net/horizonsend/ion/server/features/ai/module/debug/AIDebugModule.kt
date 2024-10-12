package net.horizonsend.ion.server.features.ai.module.debug

import BasicSteeringModule
import ContextMap

import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.custom.items.CustomItems
import net.horizonsend.ion.server.features.starship.active.ActiveStarship

import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Color
import org.bukkit.Particle


import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class AIDebugModule(controller : AIController ) : AIModule(controller) {


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
	private fun createAIShipDebug () : MutableList<VectorDisplay> {
		val mod = controller.getModuleByType<BasicSteeringModule>()?:return mutableListOf()
		val output = mutableListOf<VectorDisplay>()
		output.addAll(displayContext( mod.contexts["movementInterest"]!!,
			CustomItems.DEBUG_LINE_GREEN.constructItemStack(),controller.starship, Vector(0.0,10.2, 0.0)))
		output.addAll(displayContext( mod.contexts["shieldAwareness"]!!,
			CustomItems.DEBUG_LINE_RED.constructItemStack(),controller.starship, Vector(0.0,10.0,0.0)))
		output.add(VectorDisplay(mod::getThrust,
			CustomItems.DEBUG_LINE_BLUE.constructItemStack(), controller.starship, Vector(0.0,10.4,0.0)))
		return output
	}

	private fun displayContext(context : ContextMap,
							   model : ItemStack,
							   identifier: ActiveStarship, offset : Vector) : List<VectorDisplay>{

		return (0 until ContextMap.NUMBINS).map { i ->
			val dir = ContextMap.bindir[i]
			val display = VectorDisplay(context, i,model, identifier, offset)
			display
		}
	}

	private fun renderDebug() {
		if (!rendered) {
			displayVectors = createAIShipDebug()
			rendered =true
		}
		displayVectors.forEach { it.update() }
		val mod = controller.getModuleByType<BasicSteeringModule>()?:return
		for (player in controller.starship.world.players) {

			val particle = Particle.REDSTONE
			val dustOptions = Particle.DustOptions(Color.BLUE, 4f,)
			val orbitTarget = mod.orbitTarget
			if (orbitTarget != null) {
				player.spawnParticle(particle, orbitTarget.x, orbitTarget.y, orbitTarget.z,
					1, 0.0, 0.0, 0.0, 0.0, dustOptions)
			}
		}
	}

}
