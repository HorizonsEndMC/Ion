package net.horizonsend.ion.server.features.ai.module.debug

import com.mojang.math.Transformation
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.ai.module.steering.context.ContextMap
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.util.Brightness
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftItemDisplay
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID

class VectorDisplay private constructor(
	val vecDeg:Box,
	val model: ItemStack,
	val parent: ActiveStarship,
	val offset : Vector = Vector(0.0,10.0,0.0)
){
	val entity : ItemDisplay = createEntity()
	val mag : Double get() = vecDeg.getMag()
	val dir : Vector get() = vecDeg.getDir()

	var shownPlayers = mutableSetOf<UUID>()
	var transformation = Transformation(Vector3f(0f),Quaternionf(),Vector3f(0f),Quaternionf())

	constructor(dirSupp : () -> Vector,
				model : ItemStack,
				parent: ActiveStarship,
				offset: Vector) : this(
		functionVectorBox(dirSupp), model, parent, offset) {}

	constructor(map : ContextMap,
				binIndex : Int,
				model : ItemStack,
				parent: ActiveStarship,
				offset: Vector) : this(
		ContextVectorBox(map,binIndex), model, parent, offset) {}

	fun createEntity(): ItemDisplay {
		val offset = calcOffset()
		val entity = ItemDisplay(EntityType.ITEM_DISPLAY, parent.world.minecraft).apply {
			itemStack = CraftItemStack.asNMSCopy(model)
			viewRange = 5.0f
			this.brightnessOverride = Brightness.FULL_BRIGHT
			this.transformationInterpolationDuration = 0

			transformation = Transformation(
				offset.toVector3f(),
				ClientDisplayEntities.rotateToFaceVector(dir.clone().normalize().multiply(-1.0).toVector3f()),
				Vector3f(1f,1f,mag.toFloat()),
				Quaternionf()
			)
		}
		return entity
	}

	private fun updateEntity() {
		val offset = calcOffset()
		val transformation = com.mojang.math.Transformation(
			Vector3f(0f),
			ClientDisplayEntities.rotateToFaceVector(dir.clone().normalize().multiply(-1.0).toVector3f()),
			Vector3f(1f,1f,mag.toFloat()),
			Quaternionf()
		)
		this.transformation = transformation
		entity.teleportTo(offset.x, offset.y, offset.z)

		entity.setTransformation(transformation)
	}

	fun update() {
		updateEntity()
		val chunk = entity.level().world.getChunkAtIfLoaded(entity.x.toInt().shr(4), entity.z.toInt().shr(4)) ?: return
		val playerChunk = chunk.minecraft.playerChunk ?: return

		val viewers = playerChunk.getPlayers(false).toSet()
		val newPlayers = viewers.filterNot { shownPlayers.contains(it.uuid) }
		val old = viewers.filter { shownPlayers.contains(it.uuid) }

		for (player in newPlayers) {
			broadcast(player)
		}

		for (player in old) {
			update(player)
		}

		shownPlayers = viewers.mapTo(mutableSetOf()) { it.uuid }
	}

	private fun update(player: ServerPlayer) {
		entity.refreshEntityData(player)
		ClientDisplayEntities.moveDisplayEntityPacket(player,entity,entity.x, entity.y, entity.z)
		ClientDisplayEntities.transformDisplayEntityPacket(player.bukkitEntity,entity, transformation)
	}

	private fun broadcast(player: ServerPlayer) {
		ClientDisplayEntities.sendEntityPacket(player.bukkitEntity, entity)
		shownPlayers.add(player.uuid)
	}

	private fun calcOffset(): Vector {
		val com = parent.centerOfMass.toVector()
		com.add(offset)
		return com
	}

	abstract class Box() {
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

	class functionVectorBox(val func : () -> Vector) : Box() {
		override fun getMag(): Double {
			return func().length()
		}

		override fun getDir(): Vector {
			return func()
		}
	}

}
