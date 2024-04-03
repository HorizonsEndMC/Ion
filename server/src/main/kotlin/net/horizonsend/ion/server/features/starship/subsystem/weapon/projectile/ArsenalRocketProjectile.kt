package net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile

import net.horizonsend.ion.common.utils.miscellaneous.d
import net.horizonsend.ion.common.utils.miscellaneous.randomInt
import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.miscellaneous.utils.vectorToPitchYaw
import net.minecraft.network.protocol.game.ClientboundSetEntityDataPacket
import net.minecraft.network.syncher.EntityDataAccessor
import net.minecraft.network.syncher.EntityDataSerializers
import net.minecraft.network.syncher.SynchedEntityData
import net.minecraft.world.entity.Display.ItemDisplay
import org.bukkit.Bukkit
import org.bukkit.Location
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.block.Block
import org.bukkit.craftbukkit.v1_20_R3.entity.CraftPlayer
import org.bukkit.entity.Entity
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Transformation
import org.bukkit.util.Vector
import org.joml.Matrix4f
import org.joml.Quaternionf
import org.joml.Vector3f

class ArsenalRocketProjectile(
	starship: ActiveStarship?,
	loc: Location,
	dir: Vector,
	shooter: Damager,
) : SimpleProjectile(starship, loc, dir, shooter), DisplayEntityProjectile {
	override val balancing: StarshipWeapons.ProjectileBalancing = starship?.balancing?.weapons?.arsenalMissile!!
	override val range: Double = balancing.range
	override val speed: Double = balancing.speed
	override val starshipShieldDamageMultiplier = balancing.starshipShieldDamageMultiplier
	override val areaShieldDamageMultiplier: Double = balancing.areaShieldDamageMultiplier
	override val explosionPower: Float = balancing.explosionPower
	override val volume: Int = balancing.volume
	override val pitch: Float = balancing.pitch
	override val soundName: String = balancing.soundName
	override var displayEntity: ItemDisplay? = null
	override val originLocation: Location = loc
	override var oldLocation: Location = originLocation
	var age: Int = 0 //Ticks

	override fun tick() {
		delta = (System.nanoTime() - lastTick) / 1_000_000_000.0 // Convert to seconds
		//Only difference between these two is that for the first 2 seconds, the missile will try to go up to clear the silo and ship
		if (age< randomInt(5,12)){
			age++
			val predictedNewLoc = loc.clone().add(0.0, delta * speed/2, 0.0)
			if (!predictedNewLoc.isChunkLoaded) {
				destroyDisplayEntity()
				return
			}
			//We're not doing any impact stuff as we dont want the projectile to be able to hit stuff at this stage
			val travel = loc.distance(predictedNewLoc)

			moveVisually(loc, predictedNewLoc, travel)

			loc = predictedNewLoc

			distance += travel

			if (distance >= range) {
				destroyDisplayEntity()
				return
			}

			lastTick = System.nanoTime()
			reschedule()
		}
		else {
			super.tick()
		}
	}

	override fun moveVisually(oldLocation: Location, newLocation: Location, travel: Double) {
		newLocation.world.spawnParticle(Particle.SMOKE_LARGE, newLocation, 3)
		updateDisplayEntity(newLocation, dir.clone().normalize().multiply(speed))
	}

	override fun fire() {
		makeDisplayEntity()
		super.fire()
	}

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		destroyDisplayEntity()
		super.impact(newLoc, block, entity)
	}

	override fun updateDisplayEntity(newLocation: Location, velocity: Vector) {
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
		val trans = com.mojang.math.Transformation(
			translation,
			leftRotation,
			Vector3f(1.0f, 1.0f, 2.0f),
			Quaternionf()
		)
		displayEntity?.setTransformation(trans)
		val packet = ClientboundSetEntityDataPacket(displayEntity?.id ?: return,
			displayEntity?.entityData?.packDirty()!!
		)

		Bukkit.getServer().onlinePlayers.forEach {
			(it as CraftPlayer).handle.connection.send(packet)
			displayEntity?.entityData?.refresh(it.handle)
		}
	}
}

