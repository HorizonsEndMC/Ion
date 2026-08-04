package net.horizonsend.ion.server.features.starship.subsystem.weapon

import net.horizonsend.ion.server.configuration.starship.StarshipParticleProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom
import java.util.function.Supplier

abstract class TurretWeaponSubsystem<T : StarshipTurretWeaponBalancing<Z>, Z : StarshipParticleProjectileBalancing>(
    ship: ActiveStarship,
    pos: Vec3i,
    override var face: BlockFace,
	balancingSupplier: Supplier<T>
) : BalancedWeaponSubsystem<T>(ship, pos, balancingSupplier), DirectionalSubsystem, ManualWeaponSubsystem {
	private fun getSign() = starship.world.getBlockAt(pos.x, pos.y, pos.z).getState(false) as? Sign

	protected open val inaccuracyRadians: Double get() = Math.toRadians(balancing.inaccuracyDegrees)

	protected abstract val multiblock: TurretMultiblock<Z>

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
		return target.clone()
			.subtract(pos.toVector())
			.subtract(multiblock.getMeanFirePoint(face).toCenterVector())
			.normalize()
			.rotateAroundX(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
			.rotateAroundY(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
			.rotateAroundZ(ThreadLocalRandom.current().nextDouble(-inaccuracyRadians, inaccuracyRadians))
	}

	override fun manualFire(
        shooter: Damager,
        dir: Vector,
        target: Vector
	) {
		multiblock.shoot(starship.world, pos, face, dir, starship, shooter, this, false)
	}
}
