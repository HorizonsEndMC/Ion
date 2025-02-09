package net.horizonsend.ion.server.features.ai.module.debug

import com.mojang.math.Transformation
import net.horizonsend.ion.server.features.ai.module.steering.context.ContextMap
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.modular.display.DisplayPlayerManager
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f

class VectorDisplay private constructor(
	val vecDeg: Box,
	val item: ItemStack,
	val parent: ActiveStarship,
	val offset : Vector = Vector(0.0,10.0,0.0)
) {
	val entity : ItemDisplay = createEntity()
	private val playerManager = DisplayPlayerManager(entity)

	private val mag : Double get() = vecDeg.getMag()
	private val dir : Vector get() = vecDeg.getDir()

	constructor(directionSupplier: () -> Vector,
				item : ItemStack,
				parent: ActiveStarship,
				offset: Vector
	) : this(FunctionVectorBox(directionSupplier), item, parent, offset)

	constructor(map : ContextMap,
				binIndex : Int,
				item : ItemStack,
				parent: ActiveStarship,
				offset: Vector
	) : this(ContextVectorBox(map, binIndex), item, parent, offset)

	private fun createEntity(): ItemDisplay {
		val offset = calcOffset()
		val entity = ItemDisplay(EntityType.ITEM_DISPLAY, parent.world.minecraft).apply {
			itemStack = CraftItemStack.asNMSCopy(item)
			viewRange = 5.0f

			this.brightnessOverride = Brightness.FULL_BRIGHT
			this.transformationInterpolationDuration = 0

			setTransformation(createTransformation())

		}

		return entity
	}

	fun update() {
		val transformation = Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector(dir.clone().normalize().multiply(-1.0).toVector3f()),
			Vector3f(1f,1f,mag.toFloat()),
			Quaternionf()
		)

		entity.setTransformation(transformation)
		playerManager.runUpdates()

		val offset = calcOffset()
		entity.teleportTo(offset.x, offset.y, offset.z)
		playerManager.sendTeleport()
	}

	private fun createTransformation(): Transformation = Transformation(
		offset.toVector3f(),
		ClientDisplayEntities.rotateToFaceVector(dir.clone().normalize().multiply(-1.0).toVector3f()),
		Vector3f(1f,1f,mag.toFloat()),
		Quaternionf()
	)

	private fun calcOffset(): Vector {
		val com = parent.centerOfMass.toVector()
		com.add(offset)

		return com
	}

	abstract class Box {
		abstract fun getMag() : Double
		abstract fun getDir() : Vector
	}

	class ContextVectorBox(val map: ContextMap, val index: Int) : Box() {
		override fun getMag(): Double {
			return map.bins[index]
		}

		override fun getDir(): Vector {
			return ContextMap.bindir[index]
		}

	}

	class FunctionVectorBox(val func: () -> Vector) : Box() {
		override fun getMag(): Double {
			return func().length()
		}

		override fun getDir(): Vector {
			return func()
		}
	}
}
