package net.horizonsend.ion.server.features.starship.mininglaser

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblock
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.active.ActiveStarships
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.alongVector
import net.starlegacy.util.getFacing
import net.starlegacy.util.randomDouble
import net.starlegacy.util.rightFace
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Guardian
import org.bukkit.entity.Player
import org.bukkit.scheduler.BukkitRunnable
import org.bukkit.scheduler.BukkitTask
import org.bukkit.util.Vector
import java.lang.Math.toRadians

class MiningLaserSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	private val face: BlockFace,
	val multiblock: MiningLaserMultiblock
) : WeaponSubsystem(starship, pos), ManualWeaponSubsystem {
	val trackedGuardians = mutableListOf<Guardian>()
	val trackedTargets = mutableListOf<ArmorStand>()
	private val firingTasks = mutableListOf<BukkitTask>()
	var isFiring = false
	var target: Vector? = null

	override val powerUsage: Int = 0
	// override fun getAdjustedDir(dir: Vector, target: Vector?): Vector = target?.subtract(getFirePos().toVector()) ?: dir

	override fun getAdjustedDir(dir: Vector, target: Vector?): Vector {
		if (target != null) {
			val origin = getFirePos().toCenterVector()
			val adjustedDir = target.clone().subtract(origin)

			val horizontalAxis = adjustedDir.clone()
			horizontalAxis.y = 0.0
			horizontalAxis.rotateAroundY(90.0)
			horizontalAxis.normalize()

			adjustedDir.rotateAroundAxis(horizontalAxis, toRadians(randomDouble(-0.01, 0.01)))
			adjustedDir.rotateAroundY(toRadians(randomDouble(-0.01, 0.01)))

			return adjustedDir.normalize()
		}

		return dir
	}

	override fun canFire(dir: Vector, target: Vector?): Boolean {
		return !starship.isInternallyObstructed(getFirePos(), dir)
	}

	private fun getFirePos(): Vec3i {
		val (x, y, z) = multiblock.getFirePointOffset()
		val facing = getSign()?.getFacing() ?: face
		val right = facing.rightFace

		return Vec3i(
			x = (right.modX * x) + (facing.modX * z),
			y = (multiblock.upDownFace().direction.y * y).toInt(),
			z = (right.modZ * x) + (facing.modZ * z)
		)
	}

	private fun getSign() = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	private fun getPoints(axis: Vector): List<Location> {
		val spread: Double = 360.0 / multiblock.beamCount
		val points = mutableListOf<Location>()
		val start = axis.normalize().rotateAroundZ(90.0).multiply(multiblock.circleRadius).add(axis)

		for (count in multiblock.beamCount.downTo(1)) {
			val newLoc = start.rotateAroundNonUnitAxis(axis, spread * count)
			points.add(newLoc.toLocation(starship.serverLevel.world))
		}

		return points
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector?) {
		if (!isFiring) { startFiringSequence() } else firingTasks.forEach { it.cancel() }; firingTasks.clear()
		isFiring = !isFiring
		this.target = target
		shooter.information("Toggled mining laser! : $isFiring")
	}

	private fun startFiringSequence() {
		val fireTask = object : BukkitRunnable() {
			override fun run() {
				if (isFiring) {
					fire()
				} else {
					cancel()
				}
			}
		}.runTaskTimer(Ion, 0L, 20L)

		firingTasks.add(fireTask)
	}

	fun fire() {
		if (!ActiveStarships.isActive(starship)) {
			firingTasks.forEach { it.cancel() }
			return
		}

		val intialPos = getFirePos().toLocation(starship.serverLevel.world).toCenterLocation().add(pos.toVector())
		val adjustedVector = getAdjustedDir(pos.toVector(), target)
		val points: List<Location> = getPoints(getAdjustedDir(pos.toVector(), target))

		println(intialPos)
		println(adjustedVector)
		println(target)

		for (loc in intialPos.toLocation(starship.serverLevel.world).alongVector(getAdjustedDir(pos.toVector(), target).normalize().multiply(multiblock.range), 300)) {
			starship.serverLevel.world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}

		MiningLaserProjectile(starship, this, intialPos, points, getAdjustedDir(pos.toVector(), target)).fire()
	}
}
