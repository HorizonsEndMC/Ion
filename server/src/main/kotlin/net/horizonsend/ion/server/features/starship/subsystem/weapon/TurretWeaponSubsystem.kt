package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.multiblock.starshipweapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom

abstract class TurretWeaponSubsystem(
    ship: ActiveStarship,
    pos: Vec3i,
    override var face: BlockFace
) : WeaponSubsystem(ship, pos), DirectionalSubsystem, ManualWeaponSubsystem {
	private fun getSign() = starship.serverLevel.world.getBlockAtKey(pos.toBlockKey()).getState(false) as? Sign

	protected abstract val multiblock: TurretMultiblock
	protected abstract val inaccuracyRadians: Double

	override fun isIntact(): Boolean {
		val sign = getSign() ?: return false
		return multiblock.signMatchesStructure(sign, loadChunks = true, particles = false)
	}

	private fun getFirePoints(): List<Vec3i> = multiblock.getFirePoints(face)
		.map { Vec3i(it.x + pos.x + face.modX, it.y + pos.y + face.modY, it.z + pos.z + face.modZ) }

	fun ensureOriented(targetedDir: Vector): Boolean {
		val face = vectorToBlockFace(targetedDir)

		if (this.face == face) {
			return true
		}

		val sign = getSign() ?: return false
		this.face = multiblock.rotate(sign, this.face, face)
		return this.face == face
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		// return whether or not any of the fire points are not obstructed
		// (plus the parent classes's conditions)
		return getFirePoints().all { !starship.isInternallyObstructed(it, dir) }
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		if (target == null) {
			return dir
		}

		return target.clone()
			.subtract(pos.toVector())
			.subtract(multiblock.getMeanFirePoint(face).toCenterVector())
			.normalize()
			.rotateAroundX(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
			.rotateAroundY(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
			.rotateAroundZ(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
	}

	override fun manualFire(
		shooter: Controller,
		dir: Vector,
		target: Vector
	) {
		multiblock.shoot(starship.serverLevel.world, pos, face, dir, starship, shooter, false)
	}
}
