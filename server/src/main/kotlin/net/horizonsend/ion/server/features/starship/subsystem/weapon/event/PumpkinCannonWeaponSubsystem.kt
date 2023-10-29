package net.horizonsend.ion.server.features.starship.subsystem.weapon.event

import net.horizonsend.ion.server.features.multiblock.starshipweapon.event.PumpkinCannonStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActivePlayerStarship
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.controllers.Controller
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.projectile.PumpkinCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.PermissionWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.horizonsend.ion.server.miscellaneous.utils.vectorToBlockFace
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

class PumpkinCannonWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace,
	private val multiblock: PumpkinCannonStarshipWeaponMultiblock
) : WeaponSubsystem(starship, pos),
	ManualWeaponSubsystem,
	DirectionalSubsystem,
	PermissionWeaponSubsystem {
	override val permission: String = "ioncore.eventweapon"
	override val powerUsage: Int = 1000

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if ((starship as ActivePlayerStarship).pilot?.hasPermission("ioncore.eventweapon") == false) return false

		if (vectorToBlockFace(dir, includeVertical = false) != this.face) {
			return false
		}

		val firePos = getFirePos()

		for (offset in getSurroundingFaces()) {
			val origin = Vec3i(firePos.clone().add(offset.direction))

			if (starship.isInternallyObstructed(origin, dir)) {
				return false
			}
		}

		return true
	}

	private fun getSurroundingFaces(): Array<BlockFace> {
		if (face.modY == 0) {
			return arrayOf(BlockFace.SELF, face.rightFace, face.leftFace, BlockFace.UP, BlockFace.DOWN)
		}

		return arrayOf(BlockFace.SELF, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)
	}

	private fun getFirePos(): Vector {
		return pos.toVector().add(face.direction.clone().multiply(5.0)).add(Vector(0.5, 0.5, 0.5))
	}

	override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.serverLevel.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
	}

	override fun manualFire(shooter: Controller, dir: Vector, target: Vector) {
		val origin = getFirePos().toLocation(starship.serverLevel.world)
		val projectile = PumpkinCannonProjectile(starship, origin, dir, shooter)
		projectile.fire()
	}
}
