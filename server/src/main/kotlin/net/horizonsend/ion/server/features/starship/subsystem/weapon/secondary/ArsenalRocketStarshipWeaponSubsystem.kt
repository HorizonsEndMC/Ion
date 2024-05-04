package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.customitems.CustomItems
import net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy.ArsenalRocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ArsenalRocketProjectile
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.BlockFace
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class ArsenalRocketStarshipWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace,
	private val multiblock: ArsenalRocketStarshipWeaponMultiblock,
	private val upOrDown: BlockFace
	) :
	WeaponSubsystem(starship, pos), HeavyWeaponSubsystem, ManualWeaponSubsystem, DirectionalSubsystem, AmmoConsumingWeaponSubsystem {
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.arsenalMissile
	override val powerUsage: Int = balancing.powerUsage

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)
	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		val yFactor = when(upOrDown){
			BlockFace.UP -> 1
			BlockFace.DOWN -> -1
			else -> 1
		}
		val block = pos.toLocation(starship.world).block
		val inward = block.getRelative(this.face)
		return !starship.isInternallyObstructed(Vec3i(inward.x, inward.y.plus(5*yFactor), inward.z), Vector(0,yFactor,0))
	}

	private fun getSurroundingFaces(): Array<BlockFace> {
		if (face.modY == 0) {
			return arrayOf(BlockFace.SELF, face.rightFace, face.leftFace, BlockFace.UP, BlockFace.DOWN)
		}

		return arrayOf(BlockFace.SELF, BlockFace.EAST, BlockFace.WEST, BlockFace.NORTH, BlockFace.SOUTH)
	}

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return super.isAcceptableDirection(face)
	}

	override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
	}

	private fun getFirePos(): Vector {
		val yFactor = when(upOrDown){
			BlockFace.UP -> 1
			BlockFace.DOWN -> -1
			else -> 1
		}
		val block = pos.toLocation(starship.world).block
		val inward = block.getRelative(this.face)
		return inward.location.toVector().add(Vector(0.0, 10.0*yFactor, 0.0))
	}
	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		val origin = getFirePos().toLocation(starship.world)
		val projectile = ArsenalRocketProjectile(starship, origin, dir, shooter, upOrDown)
		projectile.fire()
	}

	override fun getRequiredAmmo(): ItemStack = CustomItems.ARSENAL_MISSILE.constructItemStack()
}
