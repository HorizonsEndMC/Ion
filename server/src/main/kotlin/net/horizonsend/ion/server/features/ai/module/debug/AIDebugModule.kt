package net.horizonsend.ion.server.features.ai.module.debug


import net.horizonsend.ion.server.features.ai.module.AIModule
import net.horizonsend.ion.server.features.ai.module.steering.BasicSteeringModule
import net.horizonsend.ion.server.features.ai.module.steering.context.ContextMap
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import org.bukkit.Color
import org.bukkit.Particle
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector

class AIDebugModule(controller : AIController ) : AIModule(controller) {
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
			"obstructionDanger"
		)

		val shownContexts = mutableListOf(
			Pair("danger", DebugColor.RED),
			Pair("movementInterest", DebugColor.GREEN))

		private val debugOffsetIncrement = 0.6
		private val debugOffset = 2.0

		enum class DebugColor{WHITE, RED, BLUE, GREEN}

		var canShipsMove = true
		var canShipsRotate = true
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
	private fun createAIShipDebug () : MutableList<VectorDisplay> {
		val mod = controller.getCoreModuleByType<BasicSteeringModule>()?:return mutableListOf()
		val output = mutableListOf<VectorDisplay>()
		val shipOffset = (controller.starship.max - controller.starship.centerOfMass).y.toDouble()
		for (i in 0 until shownContexts.size) {
			val offset = i * debugOffsetIncrement + debugOffset + shipOffset
			output.addAll(displayContext( mod.contexts[shownContexts[i].first]!!,
				mapColor(shownContexts[i].second),controller.starship, Vector(0.0,  offset,0.0)))
		}
		val dirOffset = shownContexts.size * debugOffsetIncrement + debugOffset + shipOffset
		output.add(VectorDisplay(mod::thrustOut ,mapColor(DebugColor.BLUE), controller.starship, Vector(0.0,dirOffset,0.0)))
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
		val mod = controller.getCoreModuleByType<BasicSteeringModule>()?:return
		for (player in controller.starship.world.players) {

			val particle = Particle.DUST
			val dustOptions = Particle.DustOptions(Color.BLUE, 4f,)
			val orbitTarget = mod.orbitTarget
			if (orbitTarget != null) {
				player.spawnParticle(particle, orbitTarget.x, orbitTarget.y, orbitTarget.z, 1, 0.0, 0.0, 0.0, 0.0, dustOptions)
			}
		}
	}

	private fun mapColor(color : DebugColor) : ItemStack{
		 return when (color) {
			 DebugColor.RED -> CustomItemRegistry.DEBUG_LINE_RED.constructItemStack()
			 DebugColor.GREEN -> CustomItemRegistry.DEBUG_LINE_GREEN.constructItemStack()
			 DebugColor.BLUE -> CustomItemRegistry.DEBUG_LINE_BLUE.constructItemStack()
			 DebugColor.WHITE -> CustomItemRegistry.DEBUG_LINE.constructItemStack()
		}
	}



}
