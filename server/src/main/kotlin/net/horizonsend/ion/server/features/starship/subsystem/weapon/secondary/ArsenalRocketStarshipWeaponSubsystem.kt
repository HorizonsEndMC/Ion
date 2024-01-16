package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.ArsenalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArsenalRocketProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class ArsenalRocketStarshipWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	val face: BlockFace,
	private val multiblock: ArsenalRocketStarshipWeaponMultiblock
	) :
	WeaponSubsystem(starship, pos), HeavyWeaponSubsystem, ManualWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.arsenalMissile
	override val powerUsage: Int = balancing.powerUsage

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)
	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		return !starship.isInternallyObstructed(pos.plus(Vec3i(0, 10, 0)), dir)
	}

	private fun getSurroundingFaces(): Array<BlockFace> {
		if (face.modY == 0) {
			return arrayOf(BlockFace.SELF, face.rightFace, face.leftFace, BlockFace.UP, BlockFace.DOWN)
		}

		return arrayOf(BlockFace.SELF, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)
	}

	override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
	}

	private fun getFirePos(): Vector {
		return pos.toVector().add(Vector(0.0, 10.0, 0.0))
	}
	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		val origin = getFirePos().toLocation(starship.world)
		val projectile = ArsenalRocketProjectile(starship, origin, dir, shooter)
		projectile.fire()
	}
}
