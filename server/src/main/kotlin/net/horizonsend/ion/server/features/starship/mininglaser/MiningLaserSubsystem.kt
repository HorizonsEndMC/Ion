package net.horizonsend.ion.server.features.starship.mininglaser

import net.horizonsend.ion.server.features.starship.mininglaser.multiblock.MiningLaserMultiblock
import net.horizonsend.ion.server.miscellaneous.extensions.information
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.alongVector
import net.starlegacy.util.randomDouble
import org.bukkit.Location
import org.bukkit.Particle
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.ArmorStand
import org.bukkit.entity.Guardian
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.lang.Math.toRadians
import kotlin.math.acos

class MiningLaserSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	private val face: BlockFace,
	val multiblock: MiningLaserMultiblock
) : WeaponSubsystem(starship, pos), ManualWeaponSubsystem {
	val trackedGuardians = listOf<Guardian>()
	val trackedTargets = listOf<ArmorStand>()
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
		return Vec3i(
			pos.x + face.modX * multiblock.axis.first,
			pos.y + face.modY * multiblock.axis.second,
			pos.z + face.modZ * multiblock.axis.third
		)
	}

	private fun getSign() = starship.serverLevel.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	private fun getRingCenter(fireOrigin: Location): Location {
		val pitch = fireOrigin.pitch
		val elevation = toRadians((if (pitch > 0) pitch else pitch + 360).toDouble())
		val heightBump = acos(elevation) * multiblock.circleRadius

		val adjustedHeight = fireOrigin.y + heightBump

		return Location(
			starship.serverLevel.world,
			pos.x + face.modY * multiblock.axis.first.toDouble(),
			adjustedHeight,
			pos.z + face.modZ * multiblock.axis.third.toDouble()
		)
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector?) {
		isFiring != isFiring
		this.target = target
		shooter.information("Toggled mining laser! : $isFiring")
		if (!isFiring) { fire() }
	}

	fun fire() {
		val intialPos = getFirePos()
		val adjustedPos = getRingCenter(intialPos.toLocation(null))
		val adjustedVector = getAdjustedDir(this.pos.toVector(), target)

		println(intialPos)
		println(adjustedPos)
		println(adjustedVector)

		for (loc in pos.toLocation(starship.world).alongVector(adjustedVector.multiply(multiblock.range), 300)) {
			println(loc)
			starship.world.spawnParticle(Particle.SOUL_FIRE_FLAME, loc, 1, 0.0, 0.0, 0.0, 0.0, null, true)
		}

		while (isFiring && canFire(adjustedVector, target)) {
			if ((System.currentTimeMillis() % 1000) != 0.toLong()) continue
			println("Shooting again!")
			MiningLaserProjectile()
		}
	}
}
