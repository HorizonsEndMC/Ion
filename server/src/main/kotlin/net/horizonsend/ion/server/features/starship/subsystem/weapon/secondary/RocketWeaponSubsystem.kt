package net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.custom.items.CustomItemRegistry
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy.RocketStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.DirectionalSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.WeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.AmmoConsumingWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.HeavyWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.interfaces.ManualWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.RocketProjectile
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.vectorToBlockFace
import net.horizonsend.ion.server.miscellaneous.utils.leftFace
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import org.bukkit.block.BlockFace
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
	override val balancing: StarshipWeapons.StarshipWeapon = starship.balancing.weapons.rocket
	override val powerUsage: Int = balancing.powerUsage

	override val boostChargeNanos: Long = TimeUnit.SECONDS.toNanos(balancing.boostChargeSeconds)

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
		val block = pos.toLocation(starship.world).block
		val inward = if (face in arrayOf(BlockFace.UP, BlockFace.DOWN)) BlockFace.NORTH else face
		return multiblock.blockMatchesStructure(block, inward)
	}

	override fun manualFire(shooter: Damager, dir: Vector, target: Vector) {
		val origin = getFirePos().toLocation(starship.world)
		val projectile = RocketProjectile(starship, getName(), origin, this.face, shooter)
		projectile.fire()
	}

	override fun isRequiredAmmo(item: ItemStack): Boolean {
		return requireCustomItem(item, CustomItemRegistry.ARSENAL_MISSILE, 1)
	}

	override fun consumeAmmo(itemStack: ItemStack) {
		consumeItem(itemStack, 1)
	}

	override fun getName(): Component {
		return Component.text("Rocket Turret")
	}
}
