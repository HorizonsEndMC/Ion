package net.starlegacy.feature.starship.subsystem.weapon

import net.starlegacy.feature.multiblock.starshipweapon.turret.TurretMultiblock
import net.starlegacy.feature.starship.active.ActivePlayerStarship
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AutoWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.util.Vec3i
import net.starlegacy.util.vectorToBlockFace
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.util.Vector
import java.util.concurrent.ThreadLocalRandom

abstract class TurretWeaponSubsystem(
	ship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace
) : WeaponSubsystem(ship, pos), DirectionalSubsystem, ManualWeaponSubsystem, AutoWeaponSubsystem {
	private fun getSign() = starship.serverLevel.world.getBlockAtKey(pos.toBlockKey()).getState(false) as? Sign

	protected abstract val multiblock: TurretMultiblock
	protected abstract val inaccuracyRadians: Double

	override val range: Double get() = multiblock.range

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
		val blockLocation = multiblock.getPilotLoc(starship.serverLevel.world, pos.x, pos.y, pos.z, face).toBlockLocation()
		if (pos.toLocation(starship.serverLevel.world).chunk.entities.any { it.location.toBlockLocation() == blockLocation }) {
			return false
		}
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
		shooter: Player,
		dir: Vector,
		target: Vector
	) {
		multiblock.shoot(starship.serverLevel.world, pos, face, dir, starship, shooter)
	}

	override fun autoFire(target: Player, dir: Vector) {
		val shooter = (starship as? ActivePlayerStarship)?.pilot
		multiblock.shoot(starship.serverLevel.world, pos, face, dir, starship, shooter)
	}
}
