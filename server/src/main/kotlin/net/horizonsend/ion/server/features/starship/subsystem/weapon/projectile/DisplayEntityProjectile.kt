package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import com.mojang.math.Transformation
import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.server.features.nations.gui.item
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.horizonsend.ion.server.miscellaneous.utils.toBlockPos
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket
import net.minecraft.network.protocol.game.ClientboundRemoveEntitiesPacket
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData.DataValue
import net.minecraft.world.entity.Display
import net.minecraft.world.entity.EntityType
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.craftbukkit.v1_20_R3.inventory.CraftItemStack
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt


interface DisplayEntityProjectile {
	var displayEntity: Display.ItemDisplay?

	val originLocation: Location //The position where the displayEntity was spawned, this is used in the translation maths

	var oldLocation: Location

	fun updateDisplayEntity(newLocation: Location, velocity: Vector) {
		//Ripped from https://github.com/PimvanderLoos/AnimatedArchitecture/blob/master/animatedarchitecture-spigot/spigot-core/src/main/java/nl/pim16aap2/animatedarchitecture/spigot/core/animation/BlockDisplayHelper.java#L66
		//The code I stole this from used its own vector thing called a rotation, but this should work fine
		val (pitch, yaw) = vectorToPitchYaw(velocity)
		var differencePitch: Double =
			(toMinecraftAngle(getPitchBetweenLocations(originLocation, newLocation).toDouble() - pitch))//Idek
		var differenceYaw: Double = (newLocation.yaw - yaw).toDouble()

		//We get the difference in angle, no idea why, but we dont use roll, the code I stole this from did use roll, should work tho
		//the values we get are between 0-360, we need them to be between -180 to 180
		//Quaternions dont use a point in space that they try rotate too, they use pitch and yaw to figure out how much to rotate by
		//thats why its important to supply the difference in pitch and yaw, as thats how much we want to rotate it by
		differencePitch = -toMinecraftAngle(differencePitch) //minus because for some ungodly reason it was inverted
		differenceYaw = toMinecraftAngle(differenceYaw)

		//We can only use radians
		val differencePitchRadians = Math.toRadians(differencePitch).toFloat()
		val differenceYawRadians = Math.toRadians(differenceYaw).toFloat()

		//Couldnt tell you wtf is happening here, some archaic magic anyways
		val transformation = Matrix4f().translate(
			Vector3f(-0.5F, -0.5F, -0.5F)
		).rotate(fromPitchYaw(differencePitchRadians, differenceYawRadians)).translate(
			Vector3f(0.5F, 0.5F, 0.5F)
		)
		//Get the translation needed from the originalLocation
		val translation = newLocation.toVector().subtract(originLocation.toVector()).toVector3f()
			.sub(transformation.getTranslation(Vector3f())) //Get how much we need to offset the displayEntity, and do some archaic magic with the transformation
		val leftRotation = transformation.getUnnormalizedRotation(Quaternionf())
		val trans = Transformation(
			translation,
			toQuaternion(0.0, pitch.d(),-toMinecraftAngle(yaw.d())),
			Vector3f(1.0f, 1.0f, 2.0f),
			Quaternionf()
		) //Set the new transformation
		//displayEntity.interpolationDelay = 0 //How long until the new transformation takes effect
		//displayEntity.interpolationDuration =
		//	1 //This is how long it'll take the displayEntity to reach the new translation/rotation

		val list: MutableList<DataValue<*>> = ArrayList()
		list.add(DataValue.create(EntityDataAccessor(8, EntityDataSerializers.INT), 1))
		list.add(DataValue.create(EntityDataAccessor(9, EntityDataSerializers.INT), 1))
		list.add(DataValue.create(EntityDataAccessor(13, EntityDataSerializers.QUATERNION), leftRotation))
		//Right Rotation, we dont need to use it
		list.add(DataValue.create(EntityDataAccessor(14, EntityDataSerializers.QUATERNION), Quaternionf()))
		list.add(DataValue.create(EntityDataAccessor(11, EntityDataSerializers.VECTOR3), translation))
		list.add(DataValue.create(EntityDataAccessor(12, EntityDataSerializers.VECTOR3), Vector3f(1.0f, 1.0f, 2.0f)))

		displayEntity?.setTransformation(trans)
		val packet = ClientboundSetEntityDataPacket(displayEntity?.id ?: return,
			displayEntity?.entityData?.packDirty()!!
		)

		Bukkit.getServer().onlinePlayers.forEach {
			(it as CraftPlayer).handle.connection.send(packet)
			displayEntity?.entityData?.refresh(it.handle)
		}
	}

	fun fromPitchYaw(pitch: Float, yaw: Float): Quaternionf = Quaternionf().rotateY(yaw).rotateX(pitch)
	fun toMinecraftAngle(angle: Double): Double {
		var angleToUse = angle
		angleToUse %= 360.0
		if (angleToUse > 180) angleToUse -= 360.0
		if (angleToUse < -180) angleToUse += 360.0
		return angleToUse
	}

	fun getPitchBetweenLocations(from: Location, to: Location): Float {
		val direction = to.toVector().subtract(from.toVector())
		val horizontalDistance =
			sqrt(direction.x * direction.x + direction.z * direction.z)
		return Math.toDegrees(atan2(direction.y, horizontalDistance)).toFloat()
	}

	fun destroyDisplayEntity() {
		val packet = ClientboundRemoveEntitiesPacket(displayEntity?.id ?: return)
		Bukkit.getServer().onlinePlayers.forEach { (it as CraftPlayer).handle.connection.send(packet) }
	}

	fun makeDisplayEntity() {
		for (playerBukkit in Bukkit.getServer().onlinePlayers) {
			val player = (playerBukkit as CraftPlayer)
			val connection = player.handle.connection
			val itemDisplay = Display.ItemDisplay(EntityType.ITEM_DISPLAY, player.minecraft.level()).apply {
				this.itemStack = CraftItemStack.asNMSCopy(item(Material.DRAGON_HEAD))
				setPos(originLocation.toBlockPos().center)
			}
			this.displayEntity = itemDisplay

			connection.send(ClientboundAddEntityPacket(itemDisplay))
			itemDisplay.entityData.refresh(player.handle)
		}
	}


	fun toQuaternion(roll: Double, pitch: Double, yaw: Double): Quaternionf{
		val cr = cos(Math.toRadians(roll)*0.5)
		val sr = sin(Math.toRadians(roll) * 0.5)
		val cp = cos(Math.toRadians(pitch) * 0.5)
		val sp = sin(Math.toRadians(pitch) * 0.5)
		val cy = cos(Math.toRadians(yaw) * 0.5)
		val sy = sin(Math.toRadians(yaw) * 0.5)
		return Quaternionf(
			sr*cp*cy - cr*sp*sy,
			cr*sp*cy + sr*cp*sy,
			cr*cp*sy - sr*sp*cy,
			cr*cp*cy + sr*sp*sy
		)
	}
}
