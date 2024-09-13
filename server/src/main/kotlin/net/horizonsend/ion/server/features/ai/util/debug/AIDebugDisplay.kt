package net.horizonsend.ion.server.features.ai.util.debug

import BasicSteeringModule
import ContextMap
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.inventory.ItemStack

object AIDebugDisplay : IonServerComponent() {
	// How often the planet display entities should update in ticks
	private const val UPDATE_RATE = 2L


	// These vars are for saving the info of the closest
	private var registeredShips = mutableMapOf<ActiveStarship, List<VectorDisplay>>()

	/**
	 * Runs when the server starts. Schedules a task to render the planet entities for each player.
	 */
	override fun onEnable() {
		Tasks.syncRepeat(0L, UPDATE_RATE) {
			renderDebug()
		}
	}

	/**
	 * Creates a client-side ItemDisplay entity for rendering planet icons in space.
	 * @return the NMS ItemDisplay object
	 * @param player the player that the entity should be visible to
	 * @param identifier the string used to retrieve the entity later
	 * @param distance the distance that the planet is to the player
	 * @param direction the direction that the entity will render from with respect to the player
	 */
	private fun createAIShipDebug (
		identifier: ActiveStarship,
	) : MutableList<VectorDisplay> {
		val mod = (identifier.controller as AIController).getModuleByType<BasicSteeringModule>()?:return mutableListOf()
		val output = mutableListOf<VectorDisplay>()
		output.addAll(displayContext( mod.movementInterest, CustomItems.ENERGY_SWORD_GREEN.singleItem(),identifier))
		output.addAll(displayContext( mod.danger, CustomItems.ENERGY_SWORD_RED.singleItem(),identifier))
		output.add(VectorDisplay(mod.thrustOut,mod.thrustOut::length,CustomItems.ENERGY_SWORD_BLUE.singleItem(), identifier))
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
		for (ship in ActiveStarships.all()) {
			if (ship.controller !is AIController) continue
			(ship.controller as AIController).getModuleByType<BasicSteeringModule>()?:continue
			println("boop1")
			if (ship !in registeredShips) {
				registeredShips[ship] = createAIShipDebug(ship)
			}
		}

		//TODO fully handle the removal procces
		registeredShips = registeredShips.filterKeys { it in ActiveStarships.all() } as MutableMap<ActiveStarship, List<VectorDisplay>>
		registeredShips.forEach {ship,displays -> displays.forEach() {it.update()} }
	}

}
