package net.horizonsend.ion.server.features.starship.control.weaponry

import net.horizonsend.ion.server.command.admin.debug
import net.horizonsend.ion.server.command.admin.debugBanner
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.damager.PlayerDamager
import net.horizonsend.ion.server.features.starship.subsystem.misc.MiningLaserSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.BalancedWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.FiredSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons.ManualQueuedShot
import net.horizonsend.ion.server.features.starship.subsystem.weapon.StarshipWeapons.fireQueuedShots
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.PerDamagerCooldown
import net.horizonsend.ion.server.miscellaneous.utils.isLava
import net.horizonsend.ion.server.miscellaneous.utils.isWater
import org.bukkit.FluidCollisionMode
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
		lightWeapons: Boolean,
		facing: BlockFace,
		dir: Vector,
		target: Vector,
		weaponSet: String?,
		manual: Boolean = true
	) {
		starship.debug("Common manual firing")

		starship.customTurrets.forEach { turret ->
			if (!turret.isIntact()) return@forEach
			if (weaponSet != null) {
				if (!starship.weaponSets[weaponSet].contains(turret)) return@forEach
			}

			turret.orientToTarget(dir)
		}

		val weapons = (if (weaponSet == null) starship.weapons else starship.weaponSets[weaponSet]).shuffled(ThreadLocalRandom.current())

		starship.debug("Weapons: ${weapons.joinToString { it.javaClass.simpleName }}")

		val fireTask = {
			val queuedShots = queueShots(shooter, weapons, lightWeapons, facing, dir, target, manual)
			starship.debug("Queued shots: ${queuedShots.joinToString { it.weapon.javaClass.simpleName }}")
			fireQueuedShots(queuedShots, starship)
		}

		if (!lightWeapons) {
			if (weapons.all { it !is HeavyWeaponSubsystem }) return //prevent light weapons from messing up the cooldown
			cooldown.tryExec(shooter, fireTask)
		} else fireTask()
	}

	fun getTarget(loc: Location, dir: Vector, starship: ActiveStarship, defaultDistance: Int = 500): Vector {
		starship.world.rayTraceBlocks(loc, dir.clone().normalize(), defaultDistance.toDouble(), FluidCollisionMode.NEVER, true) { !starship.contains(it.x, it.y, it.z) }?.hitPosition?.let { vector -> return vector }

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

	private fun queueShots(
		shooter: Damager,
		weapons: List<FiredSubsystem>,
		lightWeapons: Boolean,
		facing: BlockFace,
		dir: Vector,
		target: Vector,
		manual : Boolean,
	): LinkedList<ManualQueuedShot> {
		val queuedShots = LinkedList<ManualQueuedShot>()

		shooter.starship?.debugBanner("Queuing shots")

		for (weapon: FiredSubsystem in weapons) {
			shooter.starship?.debug("Weapon: ${weapon.javaClass.simpleName}")

			if (weapon !is ManualWeaponSubsystem) {
				shooter.starship?.debug("Continue, weapon cannot be manually fired.")
				continue
			}

			if ((weapon is AutoWeaponSubsystem == manual) and (shooter !is PlayerDamager)) {
				shooter.starship?.debug("Trying to manually fire an auto weapon or vice versa")
				continue
			}

			if ((weapon is MiningLaserSubsystem) and (shooter !is PlayerDamager)) {
				shooter.starship?.debug("AI ships dont fire mining lasers")
				continue
			}

			if (!weapon.isAcceptableDirection(facing)) {
				shooter.starship?.debug("Continue, weapon cannot fire in this direction.")
				continue
			}

			if (weapon is HeavyWeaponSubsystem != !lightWeapons) {
				shooter.starship?.debug("Continue, not correct click")
				continue
			}

			if (weapon is BalancedWeaponSubsystem<*> && !weapon.isCooledDown()) {
				shooter.starship?.debug("Continue, weapon not cooled down")
				continue
			}

			if (!weapon.isIntact()) {
				shooter.starship?.debug("Continue, weapon not intact")
				continue
			}

			val targetedDir: Vector = weapon.getAdjustedDir(dir, target)

			if (weapon is TurretWeaponSubsystem<*, *> && !weapon.ensureOriented(targetedDir)) {
				shooter.starship?.debug("Continue, turret not oriented properly")
				continue
			}

			if (!weapon.canFire(targetedDir, target)) {
				shooter.starship?.debug("Continue, weapon cannot fire.")
				continue
			}

			queuedShots.add(ManualQueuedShot(weapon, shooter, targetedDir, target))
		}

		shooter.debugBanner("Queuing shots end")

		return queuedShots
	}
}
