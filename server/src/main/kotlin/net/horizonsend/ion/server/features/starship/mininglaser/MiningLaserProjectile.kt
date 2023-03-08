package net.horizonsend.ion.server.features.starship.mininglaser

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.legacy.starshipweapon.projectile.RayTracedProjectile
import net.horizonsend.ion.server.miscellaneous.castSpawnEntity
import net.starlegacy.feature.starship.active.ActiveStarship
import org.bukkit.FluidCollisionMode
import org.bukkit.Location
import org.bukkit.block.Block
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Entity
import org.bukkit.entity.EntityType
import org.bukkit.entity.Guardian
import org.bukkit.entity.LivingEntity
import org.bukkit.util.Vector

class MiningLaserProjectile(
	starship: ActiveStarship?,
	val subsystem: MiningLaserSubsystem,
	loc: Location,
	val points: List<Location>,
	dir: Vector
) : RayTracedProjectile(starship, loc, dir, null) {
	override val range: Double = 300.0
	override var speed: Double = 1.0
	override val shieldDamageMultiplier: Int = 0
	override val thickness: Double = 0.0
	override val explosionPower: Float = 0f
	override val volume: Float = 10f
	override val pitch: Float = IonServer.balancing.starshipWeapons.cthulhuBeam.pitch
	override val soundName: String = IonServer.balancing.starshipWeapons.cthulhuBeam.soundName
	var guardians: MutableList<Guardian> = mutableListOf()
	var target: LivingEntity? = null

	override fun fire() {
		for (point in points) {
			point.world.spawnEntity(point, EntityType.LIGHTNING)
		}
		super.fire()
	}

	override fun tick() {
		val result = loc.world.rayTrace(loc, dir, range, FluidCollisionMode.NEVER, true, 0.1, null)

		val targetLocation = result?.hitPosition?.toLocation(loc.world) ?: return

		val block: Block? = result.hitBlock
		val entity: Entity? = result.hitEntity

		visualize(loc, targetLocation)
		if (tryImpact(result)) impact(targetLocation, block, entity)
	}

	override fun visualize(loc: Location, targetLocation: Location) {
		val stand =
			targetLocation.world.castSpawnEntity<ArmorStand>(targetLocation, EntityType.ARMOR_STAND).apply {
				isInvisible = true
				isInvulnerable = true
				setGravity(false)
				setAI(false)
			}

		this.target = stand
		points.forEach { location ->
			guardians.add(
				location.world.castSpawnEntity<Guardian>(location, EntityType.GUARDIAN).apply {
					target = stand
					isInvisible = true
					isInvulnerable = true

					setLaser(true)
					setAI(false)
					setGravity(false)
				}
			)
		}
	}

	override fun impact(newLoc: Location, block: Block?, entity: Entity?) {
		val world = newLoc.world
	}
}
