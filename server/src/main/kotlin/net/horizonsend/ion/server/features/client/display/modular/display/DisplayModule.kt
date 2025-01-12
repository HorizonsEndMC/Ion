package net.horizonsend.ion.server.features.client.display.modular.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.display.modular.TextDisplayHandler
import net.horizonsend.ion.server.miscellaneous.utils.axis
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Axis.Y
import org.bukkit.Color
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftTextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

abstract class DisplayModule(
	val handler: TextDisplayHandler,

	private val offsetRight: Double,
	private val offsetUp: Double,
	private val offsetForward: Double,

	val scale: Float
) {
	val entity: TextDisplay = createEntity()
	private val playerManager: DisplayPlayerManager = DisplayPlayerManager(entity)

	open fun createEntity(): TextDisplay {
		val craftEntity = CraftTextDisplay(
			IonServer.server as CraftServer,
			TextDisplay(EntityType.TEXT_DISPLAY, handler.holder.handlerGetWorld().minecraft)
		)

		craftEntity.billboard = org.bukkit.entity.Display.Billboard.FIXED
		craftEntity.viewRange = 5.0f
		craftEntity.brightness = org.bukkit.entity.Display.Brightness(15, 15)
		craftEntity.teleportDuration = 0
		craftEntity.backgroundColor = Color.fromARGB(0x00000000)
		craftEntity.alignment = org.bukkit.entity.TextDisplay.TextAlignment.CENTER

		craftEntity.transformation = Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector(handler.facing.direction.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		)

		val location = getLocation()
		println("Location: $location")

		return craftEntity.getNMSData(location.x, location.y, location.z)
	}

	fun getLocation(): Location {
		val rightFace = if (handler.facing.axis == Y) BlockFace.NORTH else handler.facing.rightFace

		val offsetX = (rightFace.modX * offsetRight) + (handler.facing.modX * offsetForward)
		val offsetY = offsetUp
		val offsetZ = (rightFace.modZ * offsetRight) + (handler.facing.modZ * offsetForward)

		val parentLoc = handler.getLocation()

		return Location(
			handler.holder.handlerGetWorld(),
			parentLoc.x + offsetX,
			parentLoc.y + offsetY,
			parentLoc.z + offsetZ
		)
	}

	fun resetPosition() {
		val location = getLocation()

		entity.teleportTo(location.x, location.y, location.z)
		entity.setTransformation(com.mojang.math.Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector2d(handler.facing.direction.toVector3f()),
			Vector3f(scale),
			Quaternionf()
		))

		playerManager.runUpdates()
		playerManager.sendTeleport()
	}

	/** Registers this display handler */
	abstract fun register()

	/** Registers this display handler */
	abstract fun deRegister()

	abstract fun getText(): Component

	private fun setText(text: Component) {
		entity.text = PaperAdventure.asVanilla(text)
	}

	open fun remove() {
		playerManager.sendRemove()
	}

	open fun display() {
		runUpdates()
	}

	open fun runUpdates() {
		val newText = getText()
		setText(newText)

		playerManager.runUpdates()
	}
}
