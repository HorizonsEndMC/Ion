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
		if (age< randomInt(10,15)){
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
}

