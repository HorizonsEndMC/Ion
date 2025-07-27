package net.horizonsend.ion.server.features.client.display.modular

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayPlayerManager
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.World
import org.bukkit.block.data.BlockData
import org.bukkit.craftbukkit.CraftServer
import org.bukkit.craftbukkit.entity.CraftBlockDisplay
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf

class BlockDisplayWrapper(
	val world: World,
	initPosition: Vector,
	initHeading: Vector,
	initTransformation: Vector,
	val blockData: BlockData,
	initScale: Vector = Vector(1.0, 1.0, 1.0)
) : DisplayWrapper {

	override var position: Vector = initPosition
		set(value) {
			field = value
			updateTransformation(entity)
			entity.transformationInterpolationDuration = 3
			playerManager.sendTeleport()
		}

	override var heading: Vector = initHeading
		set(value) {
			field = value
			updateTransformation(entity)
			entity.transformationInterpolationDuration = 3
		}

	override var offset: Vector = initTransformation
		set(value) {
			field = value
			updateTransformation(entity)
			entity.transformationInterpolationDuration = 3
		}

	override var scale: Vector = initScale
		set(value) {
			field = value
			updateTransformation(entity)
			entity.transformationInterpolationDuration = 3
		}

	private var entity: Display.BlockDisplay = createEntity().getNMSData(
		position.x,
		position.y,
		position.z
	)

	val playerManager = DisplayPlayerManager(entity)

	private fun createEntity(): CraftBlockDisplay = CraftBlockDisplay(
		IonServer.server as CraftServer,
		Display.BlockDisplay(EntityType.BLOCK_DISPLAY, world.minecraft)
	).apply {
		teleportDuration = 3
		interpolationDelay = 0
		interpolationDuration = 3
		viewRange = 1000f
		brightness = org.bukkit.entity.Display.Brightness(15, 15)

		transformation = Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
			scale.toVector3f(),
			Quaternionf()
		)

		block = this@BlockDisplayWrapper.blockData
	}

	fun updateTransformation(entity: Display.BlockDisplay) {
		entity.setTransformation(com.mojang.math.Transformation(
			offset.toVector3f(),
			ClientDisplayEntities.rotateToFaceVector(heading.toVector3f()),
			scale.toVector3f(),
			Quaternionf()
		))
	}

	override fun remove() {
		playerManager.sendAllRemove()
	}

	override fun update() {
		playerManager.runUpdates()
	}

	override fun getEntity() = entity
}
