package net.starlegacy.feature.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.IonServer
import net.starlegacy.feature.misc.CustomItems
import net.starlegacy.feature.multiblock.starshipweapon.heavy.RocketStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.DirectionalSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.WeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.starlegacy.feature.starship.subsystem.weapon.projectile.RocketProjectile
import net.starlegacy.util.Vec3i
import net.starlegacy.util.leftFace
import net.starlegacy.util.rightFace
import net.starlegacy.util.vectorToBlockFace
import org.bukkit.block.BlockFace
import org.bukkit.entity.Player
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.concurrent.TimeUnit

class RocketWeaponSubsystem(
	starship: ActiveStarship,
	pos: Vec3i,
	override var face: BlockFace,
	private val multiblock: RocketStarshipWeaponMultiblock
) : WeaponSubsystem(starship, pos),
	HeavyWeaponSubsystem,
	DirectionalSubsystem,
	ManualWeaponSubsystem,
	AmmoConsumingWeaponSubsystem {
	override val powerUsage: Int = IonServer.balancing.starshipWeapons.rocket.powerUsage

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(IonServer.balancing.starshipWeapons.rocket.boostChargeNanos)

	override fun isAcceptableDirection(face: BlockFace): Boolean {
		return true
	}

	override fun getAdjustedDir(dir: Vector, target: Vector): Vector {
		return dir
	}

	override fun canFire(dir: Vector, target: Vector): Boolean {
		if (vectorToBlockFace(dir, includeVertical = true) != this.face) {
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
		return pos.toVector().add(face.direction.clone().multiply(3.0)).add(Vector(0.5, 0.5, 0.5))
	}

	override fun isIntact(): Boolean {
		val block = pos.toLocation(starship.serverLevel.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
	}

	override fun manualFire(shooter: Player, dir: Vector, target: Vector) {
		val origin = getFirePos().toLocation(starship.serverLevel.world)
		val projectile = RocketProjectile(starship, origin, this.face, shooter)
		projectile.fire()
	}

	override fun getRequiredAmmo(): ItemStack {
		return CustomItems.ROCKET_ORIOMIUM.singleItem()
	}
}
