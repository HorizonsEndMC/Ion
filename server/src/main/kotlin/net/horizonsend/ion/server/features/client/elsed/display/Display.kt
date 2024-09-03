package net.horizonsend.ion.server.features.client.elsed.display

import io.papermc.paper.adventure.PaperAdventure
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.elsed.TextDisplayHandler
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.minecraft.world.entity.Display.TextDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.Color
import org.bukkit.block.BlockFace
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftTextDisplay
import org.bukkit.util.Transformation
import org.joml.Quaternionf
import org.joml.Vector3f

abstract class Display(
	private val offsetLeft: Double,
	private val offsetUp: Double,
	private val offsetBack: Double,
	val facing: BlockFace,
	val scale: Float
) {
	protected lateinit var handler: TextDisplayHandler
	lateinit var entity: TextDisplay; private set

	fun setParent(parent: TextDisplayHandler) {
		this.handler = parent
		val rightFace = facing.rightFace

		val offsetX = rightFace.modX * offsetLeft + facing.modX * offsetBack
		val offsetY = offsetUp
		val offsetZ = rightFace.modZ * offsetLeft + facing.modZ * offsetBack

		entity = CraftTextDisplay(
			IonServer.server as CraftServer,
			TextDisplay(EntityType.TEXT_DISPLAY, parent.world.minecraft)
		).apply {
			billboard = org.bukkit.entity.Display.Billboard.FIXED
			viewRange = 5.0f
			brightness = org.bukkit.entity.Display.Brightness(15, 15)
			teleportDuration = 0
			backgroundColor = Color.fromARGB(0x00000000)
			alignment = org.bukkit.entity.TextDisplay.TextAlignment.CENTER

			transformation = Transformation(
				Vector3f(0f),
				ClientDisplayEntities.rotateToFaceVector2d(this@Display.facing.direction.toVector3f()),
				Vector3f(scale),
				Quaternionf()
			)
		}.getNMSData(parent.x + offsetX, parent.y + offsetY, parent.z + offsetZ)
	}

	/** Registers this display handler */
	abstract fun register()

	/** Registers this display handler */
	abstract fun deRegister()

	abstract fun getText(): Component

	private fun setText(text: Component) {
		entity.text = PaperAdventure.asVanilla(text)
	}

	fun display() {
		if (!::handler.isInitialized) return

		println("Displaying $this")

		setText(getText())
		handler.update(entity)
	}
}
