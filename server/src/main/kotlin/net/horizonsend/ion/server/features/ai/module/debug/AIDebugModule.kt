package net.horizonsend.ion.server.features.ai.module.debug

import BasicSteeringModule
import ContextMap
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.inventory.ItemStack

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
		output.addAll(displayContext( mod.movementInterest, CustomItems.ENERGY_SWORD_GREEN.singleItem(),controller.starship))
		output.addAll(displayContext( mod.danger, CustomItems.ENERGY_SWORD_RED.singleItem(),controller.starship))
		output.add(VectorDisplay(mod.thrustOut,mod.thrustOut::length,CustomItems.ENERGY_SWORD_BLUE.singleItem(), controller.starship))
		return output
	}

	private fun displayContext(context : ContextMap,
							   model : ItemStack,
							   identifier: ActiveStarship, ) : List<VectorDisplay>{

		return (0..ContextMap.NUMBINS).map {i ->
			val dir = ContextMap.bindir[i]
			val display = VectorDisplay(dir, i, context.bins,model, identifier)
			display
		}
	}

	private fun renderDebug() {
		if (!rendered) {
			displayVectors = createAIShipDebug()
			rendered =true
		}
		displayVectors.forEach { it.update() }
	}

}
