package net.horizonsend.ion.server.features.ai.module.debug

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntities
import net.horizonsend.ion.server.features.client.display.ClientDisplayEntityFactory.getNMSData
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.getChunkAtIfLoaded
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Display.ItemDisplay
import net.minecraft.world.entity.EntityType
import org.bukkit.craftbukkit.v1_20_R3.CraftServer
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftItemDisplay
import org.bukkit.entity.Display
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Quaternionf
import org.joml.Vector3f
import java.util.UUID
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

class VectorDisplay private constructor(
	var dir: Vector,
	val magDeg:Box,
	val model: ItemStack,
	val parent: ActiveStarship,
	val offset : Vector = Vector(0.0,10.0,0.0)
){
	val entity : ItemDisplay = createEntity()
	val mag : Double get() = magDeg.getVal()

	var shownPlayers = mutableSetOf<UUID>()
	var transformation = com.mojang.math.Transformation(Vector3f(0f),Quaternionf(),Vector3f(0f),Quaternionf())

	constructor(dir : Vector,
				model : ItemStack,
				parent: ActiveStarship,
				offset: Vector) : this(
		dir, ArrayVectorBox(dir), model, parent, offset) {}

	constructor(dir : Vector,
				binIndex : Int,
				bins : DoubleArray,
				model : ItemStack,
				parent: ActiveStarship,
				offset: Vector) : this(
		dir, ArrayPosBox(bins,binIndex), model, parent, offset) {}

	fun createEntity(): ItemDisplay {
		println("boop2")
		val entity = CraftItemDisplay(
			IonServer.server as CraftServer,
			ItemDisplay(EntityType.ITEM_DISPLAY, parent.world.minecraft)
		).apply {
			itemStack = model
			billboard = Display.Billboard.FIXED
			viewRange = 5.0f
			brightness = Display.Brightness(15, 15)
			teleportDuration = 0

			transformation = Transformation(
				Vector3f(0f),
				ClientDisplayEntities.rotateToFaceVector(dir.clone().normalize().multiply(-1.0).toVector3f()),
				Vector3f(1f,1f,mag.toFloat()),
				Quaternionf()
			)
		}
		val offset = calcOffset()
		val nmsEntity = entity.getNMSData(offset.x, offset.y, offset.z)

		return nmsEntity
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
		entity.entityData.refresh(player)
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
		abstract fun getVal() : Double
	}

	class ArrayPosBox(val array: DoubleArray, val index: Int) : Box() {
		override fun getVal(): Double {
			return array[index]
		}
	}

	class ArrayVectorBox(val vector : Vector) : Box() {
		override fun getVal(): Double {
			return vector.length()
		}
	}

}
