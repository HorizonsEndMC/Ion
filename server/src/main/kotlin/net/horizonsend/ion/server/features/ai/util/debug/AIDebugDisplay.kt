package net.horizonsend.ion.server.features.ai.util.debug

import BasicSteeringModule
import ContextMap
import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.cache.PlayerCache
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.space.Space
import net.horizonsend.ion.server.features.space.SpaceWorlds
import net.horizonsend.ion.server.features.starship.PilotedStarships
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.control.controllers.ai.AIController
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor
import org.bukkit.Bukkit
import org.bukkit.Color
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerTeleportEvent
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.math.PI
import kotlin.math.min

object AIDebugDisplay : IonServerComponent() {
	// How often the planet display entities should update in ticks
	private const val UPDATE_RATE = 10L


	// These vars are for saving the info of the closest
	private val lowestAngleMap = mutableMapOf<UUID, Float>()
	val planetSelectorDataMap = mutableMapOf<UUID, PlanetSelectorData>()
	private val entityList = mutableListOf<VectorDisplay>()

	/**
	 * Runs when the server starts. Schedules a task to render the planet entities for each player.
	 */
	override fun onEnable() {
		Tasks.syncRepeat(0L, UPDATE_RATE) {
			Bukkit.getOnlinePlayers().forEach { player ->
				renderPlanets(player)
			}
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
	private fun createAIShipDebug(
		player: Player,
		identifier: ActiveStarship,
		distance: Double,
		scaleFactor: Double = 1.0
	): Boolean {
		if (identifier.controller !is AIController) return false
		val mod = (identifier.controller as AIController).getModuleByType<BasicSteeringModule>()?:return false
		displayContext( mod.movementInterest, CustomItems.ENERGY_SWORD_GREEN.singleItem(), player,identifier,distance)
		displayContext( mod.movementInterest, CustomItems.ENERGY_SWORD_RED.singleItem(), player,identifier,distance)
		return true

	}

	private fun displayContext(context : ContextMap,
							   model : ItemStack,
							   player: Player,
							   identifier: ActiveStarship,
							   distance: Double,) {
		for (i in 0..ContextMap.NUMBINS) {
			val dir = ContextMap.bindir[i]
			val mag = context.bins[i]
			val display = VectorDisplay(identifier.centerOfMass.toVector(),dir.clone().multiply(mag),model)
			display.createEntity(player, identifier.identifier, distance)
		}
	}

	/**
	 * Updates a client-side ItemDisplay for rendering planet icons in space
	 * @param player the player that the entity should be visible to
	 * @param identifier the string used to retrieve the entity later
	 * @param distance the distance that the planet is to the player
	 * @param direction the direction that the entity will render from with respect to the player
	 * @param selectable if this entity should be selectable by the player
	 */
	private fun updatePlanetEntity(
		player: Player,
		identifier: String,
		distance: Double,
		direction: Vector,
		scaleFactor: Double = 1.0,
		selectable: Boolean = true
	) {

		val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return
		val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)

		// remove entity if it is in an unloaded chunk or different world (this causes the entity client-side to despawn?)
		// also do not render if the planet is closer than the entity render distance
		if (!nmsEntity.isChunkLoaded ||
			nmsEntity.level().world.name != player.world.name ||
			distance < entityRenderDistance * 2
		) {
			ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
			ClientDisplayEntities[player.uniqueId]?.remove(identifier)
			return
		} else {
			// calculate position and offset
			val position = player.eyeLocation.toVector()
			val scale = scale(distance, scaleFactor)
			val offset = direction.clone().normalize().multiply(entityRenderDistance + offsetMod(scale))

			// apply transformation
			val transformation = com.mojang.math.Transformation(
				offset.toVector3f(),
				ClientDisplayEntities.rotateToFaceVector2d(offset.toVector3f()),
				Vector3f(scale * ClientDisplayEntities.viewDistanceFactor(entityRenderDistance)),
				Quaternionf()
			)

			ClientDisplayEntities.moveDisplayEntityPacket(player.minecraft, nmsEntity, position.x, position.y, position.z)
			ClientDisplayEntities.transformDisplayEntityPacket(player, nmsEntity, transformation)

			if (selectable) {
				// angle in radians
				val angle = player.location.direction.angle(offset)
				// set the lowest angle vars if there is a closer entity to the player's cursor
				if (angle < PLANET_SELECTOR_ANGLE_THRESHOLD && lowestAngleMap[player.uniqueId] != null && lowestAngleMap[player.uniqueId]!! > angle) {
					lowestAngleMap[player.uniqueId] = angle
					planetSelectorDataMap[player.uniqueId] =
						PlanetSelectorData(identifier, entityRenderDistance, direction, scale)
				}
			}
		}
	}

	/**
	 * Deletes a client-side ItemDisplay planet
	 * @param player the player to delete the planet for
	 * @param identifier the identifier of the entity to delete
	 */
	private fun deletePlanetEntity(player: Player, identifier: String) {

		val nmsEntity = ClientDisplayEntities[player.uniqueId]?.get(identifier) ?: return

		ClientDisplayEntities.deleteDisplayEntityPacket(player, nmsEntity)
		ClientDisplayEntities[player.uniqueId]?.remove(identifier)
	}





	/**
	 * Equation for getting the scale of a planet display entity. Maximum (0, 100) and horizontal asymptote at x = 5.
	 * @return a scale size for planet display entities
	 * @param distance the distance at which the player is from the planet
	 * @param scaleReductionFactor adjust the rate at which the curve is reduced
	 */
	private fun scale(distance: Double, scaleReductionFactor: Double) = ((500000000 /
		((0.0625 * distance * distance * scaleReductionFactor) + 5250000)) + 5).toFloat()

	/**
	 * Equation for modifying the distance offset of a planet display entity. When added to the offset, decreases the
	 * distance by a maximum of two blocks depending on the scale of the planet.
	 * @param scale the current scale of the planet
	 */
	private fun offsetMod(scale: Float) = scale * -0.02

	/**
	 * Function for getting the distance offset that the planet selector text should be lowered by.
	 * @return the distance between the center of the planet selector and the planet selector text
	 * @param scale the scale of the planet selector text
	 * @param player the affected player
	 */
	private fun getTextOffset(scale: Float, player: Player) =
		0.48 * scale * (min(player.clientViewDistance.toDouble(), Bukkit.getWorlds()[0].viewDistance.toDouble()) / 10.0)

	/**
	 * Gets the associated custom item from the planet's name.
	 * @return the custom planet icon ItemStack
	 * @param name the name of the planet
	 */
	private fun getPlanetItemStack(name: String): ItemStack = when (name) {
		"Aerach" -> net.horizonsend.ion.server.features.custom.items.CustomItems.AERACH
		"Aret" -> net.horizonsend.ion.server.features.custom.items.CustomItems.ARET
		"Chandra" -> net.horizonsend.ion.server.features.custom.items.CustomItems.CHANDRA
		"Chimgara" -> net.horizonsend.ion.server.features.custom.items.CustomItems.CHIMGARA
		"Damkoth" -> net.horizonsend.ion.server.features.custom.items.CustomItems.DAMKOTH
		"Disterra" -> net.horizonsend.ion.server.features.custom.items.CustomItems.DISTERRA
		"Eden" -> net.horizonsend.ion.server.features.custom.items.CustomItems.EDEN
		"Gahara" -> net.horizonsend.ion.server.features.custom.items.CustomItems.GAHARA
		"Herdoli" -> net.horizonsend.ion.server.features.custom.items.CustomItems.HERDOLI
		"Ilius" -> net.horizonsend.ion.server.features.custom.items.CustomItems.ILIUS
		"Isik" -> net.horizonsend.ion.server.features.custom.items.CustomItems.ISIK
		"Kovfefe" -> net.horizonsend.ion.server.features.custom.items.CustomItems.KOVFEFE
		"Krio" -> net.horizonsend.ion.server.features.custom.items.CustomItems.KRIO
		"Lioda" -> net.horizonsend.ion.server.features.custom.items.CustomItems.LIODA
		"Luxiterna" -> net.horizonsend.ion.server.features.custom.items.CustomItems.LUXITERNA
		"Qatra" -> net.horizonsend.ion.server.features.custom.items.CustomItems.QATRA
		"Rubaciea" -> net.horizonsend.ion.server.features.custom.items.CustomItems.RUBACIEA
		"Turms" -> net.horizonsend.ion.server.features.custom.items.CustomItems.TURMS
		"Vask" -> net.horizonsend.ion.server.features.custom.items.CustomItems.VASK

		"Asteri" -> net.horizonsend.ion.server.features.custom.items.CustomItems.ASTERI
		"EdenHack" -> net.horizonsend.ion.server.features.custom.items.CustomItems.HORIZON
		"Ilios" -> net.horizonsend.ion.server.features.custom.items.CustomItems.ILIOS
		"Regulus" -> net.horizonsend.ion.server.features.custom.items.CustomItems.REGULUS
		"Sirius" -> net.horizonsend.ion.server.features.custom.items.CustomItems.SIRIUS

		else -> net.horizonsend.ion.server.features.custom.items.CustomItems.AERACH
	}.constructItemStack()

	/**
	 * Renders client-side ItemEntity planets for each player.
	 * @param player the player to send objects to
	 */
	private fun renderPlanets(player: Player) {
		// Only render planets if the player is in a space world
		if (!SpaceWorlds.contains(player.world)) return

		val planetList = Space.getPlanets().filter { it.spaceWorld == player.world }
		val playerDisplayEntities = ClientDisplayEntities[player.uniqueId] ?: return

		// Reset planet selector information
		lowestAngleMap[player.uniqueId] = Float.MAX_VALUE

		val hudPlanetsImageEnabled = PlayerCache[player].hudPlanetsImage
		val hudPlanetsSelectorEnabled = PlayerCache[player].hudPlanetsSelector

		// Rendering planets
		for (planet in planetList) {
			if (hudPlanetsImageEnabled) {
				val distance = player.location.toVector().distance(planet.location.toVector())
				val direction = planet.location.toVector().subtract(player.location.toVector()).normalize()

				if (playerDisplayEntities[planet.name] == null) {
					// entity does not exist yet; create it
					// send packet and create the planet entity
					createPlanetEntity(player, planet.name, distance, direction) ?: continue
				} else {
					// entity exists; update position
					updatePlanetEntity(player, planet.name, distance, direction)
				}
			} else if (playerDisplayEntities[planet.name] != null) {
				deletePlanetEntity(player, planet.name)
			}
		}

	}

	/**
	 * Event handler that updates HUD planets when a player teleports.
	 * @param event PlayerTeleportEvent
	 */
	@Suppress("unused")
	@EventHandler
	private fun onPlayerTeleport(event: PlayerTeleportEvent) {
		Tasks.sync {
			renderPlanets(event.player)
		}
	}

	data class PlanetSelectorData(
		val name: String,
		val distance: Int,
		val direction: Vector,
		val scale: Float
	)
}
