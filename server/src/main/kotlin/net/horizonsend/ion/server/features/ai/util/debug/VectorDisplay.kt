package net.horizonsend.ion.server.features.ai.util.debug

import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.display.PlanetSpaceRendering
import net.horizonsend.ion.server.miscellaneous.utils.toVector3f
import org.bukkit.Location
import org.bukkit.entity.Display
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class VectorDisplay (
	var pos : Location,
	var dir : Vector,
	val model : ItemStack
){
	val mag : Double get() = dir.length()
	/**
	 * Creates a client-side ItemDisplay entity for rendering planet icons in space.
	 * @return the NMS ItemDisplay object
	 * @param player the player that the entity should be visible to
	 * @param identifier the string used to retrieve the entity later
	 * @param distance the distance that the planet is to the player
	 * @param direction the direction that the entity will render from with respect to the player
	 */
	fun createEntity(
		player: Player,
		identifier: String,
		distance: Double,
	): net.minecraft.world.entity.Display.ItemDisplay? {

		/* Start with the Bukkit entity first as the NMS entity has private values that are easier to set by working off
		 * the Bukkit wrapper first */
		val entity = ClientDisplayEntityFactory.createItemDisplay(player)
		val entityRenderDistance = ClientDisplayEntities.getViewDistanceEdge(player)
		// do not render if the planet is closer than the entity render distance
		if (distance < entityRenderDistance * 2) return null

		entity.itemStack = model
		entity.billboard = Display.Billboard.FIXED
		entity.viewRange = 5.0f
		//entity.interpolationDuration = PLANET_UPDATE_RATE.toInt()
		entity.brightness = Display.Brightness(15, 15)
		entity.teleportDuration = 0

		// apply transformation
		entity.transformation = Transformation(
			pos.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector(dir.toVector3f()),
			Vector3f(mag.toFloat()),
			Quaternionf()
		)

		// position needs to be assigned immediately or else the entity gets culled as it's not in a loaded chunk
		val nmsEntity = entity.getNMSData(pos.x, pos.y, pos.z)

		ClientDisplayEntities.sendEntityPacket(player, nmsEntity)
		ClientDisplayEntities[player.uniqueId]?.set(identifier, nmsEntity)

		return nmsEntity
	}

}
