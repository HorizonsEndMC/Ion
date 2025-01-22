package net.horizonsend.ion.server.features.starship.control.weaponry

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.PerDamagerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.isLava
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import org.bukkit.Location
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector
import java.util.LinkedList
import java.util.concurrent.ThreadLocalRandom
import java.util.concurrent.TimeUnit

object StarshipWeaponry : IonServerComponent() {
	val cooldown = PerDamagerCooldown(250L, TimeUnit.MILLISECONDS)
	val rightClickTimes = mutableMapOf<Damager, Long>()

	fun manualFire(
        shooter: Damager,
        starship: ActiveStarship,
        leftClick: Boolean,
        facing: BlockFace,
        dir: Vector,
        target: Vector,
        weaponSet: String?
	) {
		starship.debug("Common manual firing")

		val weapons = (if (weaponSet == null) starship.weapons else starship.weaponSets[weaponSet]).shuffled(ThreadLocalRandom.current())

		starship.debug("Weapons: ${weapons.joinToString { it.name }}")

		val fireTask = {
			val queuedShots = queueShots(shooter, weapons, leftClick, facing, dir, target)
			starship.debug("Queued shots: ${queuedShots.joinToString { it.weapon.name }}")
			StarshipWeapons.fireQueuedShots(queuedShots, starship)
		}

		if (!leftClick) cooldown.tryExec(shooter, fireTask) else fireTask()
	}

	fun getTarget(loc: Location, dir: Vector, starship: ActiveStarship, defaultDistance: Int = 500): Vector {
		val world = loc.world
		var target: Vector = loc.toVector()
		val x = loc.blockX
		val y = loc.blockY
		val z = loc.blockZ
		for (i in 0 until 500) {
			val bx = (x + dir.x * i).toInt()
			val by = (y + dir.y * i).toInt()
			val bz = (z + dir.z * i).toInt()
			if (starship.contains(bx, by, bz)) {
				continue
			}
			if (!world.isChunkLoaded(bx shr 4, bz shr 4)) {
				continue
			}
			val type = world.getBlockAt(bx, by, bz).type
			target = Vector(bx + 0.5, by + 0.5, bz + 0.5)
			if (!type.isAir && !type.isWater && !type.isLava) {
				return target
			}
			if (world.getNearbyLivingEntities(target.toLocation(world), 0.5).any { !starship.isWithinHitbox(it) }) {
				return target
			}
		}
		// return a vector along the direction vector with the default distance
		return Vector((x + dir.x * defaultDistance) + 0.5, (y + dir.y * defaultDistance) + 0.5, (z + dir.z * defaultDistance) + 0.5)
	}

	private const val MAX_POSSIBLE_RANGE = 500
	private const val MAX_POSSIBLE_ANGLE = 0.523599 // 30 degrees in radians

	fun findPossibleTarget(loc: Location, originalTarget: Vector, starship: ActiveStarship): Vector? {
		// Get all starships CoM within range
		val targetShips = ActiveStarships.getInWorld(loc.world).filter { otherStarship ->
			otherStarship.centerOfMass.toCenterVector().distanceSquared(loc.toVector()) <= MAX_POSSIBLE_RANGE * MAX_POSSIBLE_RANGE &&
					otherStarship != starship
		}

		return targetShips.map {
			// First convert the starships to a list of vectors of CoM's position
			otherStarship -> otherStarship.centerOfMass.toCenterVector().subtract(loc.toVector())
		}.filter { comVector ->
			// Filter vectors by maximum angle
			comVector.angle(originalTarget) <= MAX_POSSIBLE_ANGLE
		}.minByOrNull { comVector ->
			// Get the vector with the smallest angle
			comVector.angle(originalTarget)
		}
	}

	fun queueShots(
        shooter: Damager,
        weapons: List<WeaponSubsystem>,
        leftClick: Boolean,
        facing: BlockFace,
        dir: Vector,
        target: Vector
	): LinkedList<StarshipWeapons.ManualQueuedShot> {
		val queuedShots = LinkedList<StarshipWeapons.ManualQueuedShot>()

		shooter.starship?.debugBanner("Queuing shots")

		for (weapon: WeaponSubsystem in weapons) {
			shooter.starship?.debug("Weapon: ${weapon.name}")

			if (weapon !is ManualWeaponSubsystem) {
				shooter.starship?.debug("Continue, weapon cannot be manually fired.")
				continue
			}

			if (!weapon.isAcceptableDirection(facing)) {
				shooter.starship?.debug("Continue, weapon cannot fire in this direction.")
				continue
			}

			if (weapon is HeavyWeaponSubsystem != !leftClick) {
				shooter.starship?.debug("Continue, not correct click")
				continue
			}

			if (!weapon.isCooledDown()) {
				shooter.starship?.debug("Continue, weapon not cooled down")
				continue
			}

			if (!weapon.isIntact()) {
				shooter.starship?.debug("Continue, weapon not intact")
				continue
			}

			val targetedDir: Vector = weapon.getAdjustedDir(dir, target)

			if (weapon is TurretWeaponSubsystem && !weapon.ensureOriented(targetedDir)) {
				shooter.starship?.debug("Continue, turret not oriented properly")
				continue
			}

			if (!weapon.canFire(targetedDir, target)) {
				shooter.starship?.debug("Continue, weapon cannot fire.")
				continue
			}

			queuedShots.add(StarshipWeapons.ManualQueuedShot(weapon, shooter, targetedDir, target))
		}

		shooter.debugBanner("Queuing shots end")

		return queuedShots
	}
}
