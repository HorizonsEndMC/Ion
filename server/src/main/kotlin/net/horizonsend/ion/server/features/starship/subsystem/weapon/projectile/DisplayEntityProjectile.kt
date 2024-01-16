package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import org.bukkit.Location
import org.bukkit.entity.EntityType
import org.bukkit.entity.ItemDisplay
import org.bukkit.entity.Player
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f
import kotlin.math.atan2
import kotlin.math.sqrt

interface DisplayEntityProjectile {
	val displayEntity: ItemDisplay

	val originLocation: Location //The position where the displayEntity was spawned, this is used in the translation maths

	var oldLocation: Location

	fun initilizeDisplayEntityValues() {}

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
		displayEntity.transformation = Transformation(
			translation,
			leftRotation,
			displayEntity.transformation.scale,
			Quaternionf()
		) //Set the new transformation
		displayEntity.interpolationDelay = 0 //How long until the new transformation takes effect
		displayEntity.interpolationDuration =
			1 //This is how long it'll take the displayEntity to reach the new translation/rotation
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
}
