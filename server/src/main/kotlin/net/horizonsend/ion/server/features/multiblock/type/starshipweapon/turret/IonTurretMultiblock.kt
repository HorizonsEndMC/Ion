package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.IonTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.IonTurretProjectile
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material.END_ROD
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

sealed class IonTurretMultiblock : TurretMultiblock() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return IonTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.ionTurret

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +3), Vec3i(1, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-3) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleslab()
				x(+1).anyStairs()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).carbyne()
				x(+0).carbyne()
				x(+1).carbyne()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleslab()
				x(+1).anyStairs()
			}
		}
		z(-1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-3).anyStairs()
				x(-2).carbyne()
				x(-1).carbyne()
				x(+0).copperBlock()
				x(+1).carbyne()
				x(+2).carbyne()
				x(+3).anyStairs()
			}
			y(getSign() * 4) {
				x(-2).anySlab()
				x(-1).terracottaOrDoubleslab()
				x(+0).terracottaOrDoubleslab()
				x(+1).terracottaOrDoubleslab()
				x(+2).anySlab()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-3).terracottaOrDoubleslab()
				x(-2).carbyne()
				x(-1).terracottaOrDoubleslab()
				x(+0).copperBlock()
				x(+1).terracottaOrDoubleslab()
				x(+2).carbyne()
				x(+3).terracottaOrDoubleslab()
			}
			y(getSign() * 4) {
				x(-2).anySlab()
				x(-1).type(GRINDSTONE)
				x(+0).anyStairs()
				x(+1).type(GRINDSTONE)
				x(+2).anySlab()
			}
		}
		z(+1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-3).anyStairs()
				x(-2).carbyne()
				x(-1).terracottaOrDoubleslab()
				x(+0).copperBlock()
				x(+1).terracottaOrDoubleslab()
				x(+2).carbyne()
				x(+3).anyStairs()
			}
			y(getSign() * 4) {
				x(-2).anySlab()
				x(-1).type(END_ROD)
				x(+0).anySlab()
				x(+1).type(END_ROD)
				x(+2).anySlab()
			}
		}
		z(+2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).terracottaOrDoubleslab()
				x(+0).carbyne()
				x(+1).terracottaOrDoubleslab()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).type(END_ROD)
				x(+0).type(IRON_TRAPDOOR)
				x(+1).type(END_ROD)
			}
		}
		z(+3) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleslab()
				x(+1).anyStairs()
			}
		}
	}

	override fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, starship: ActiveStarship, shooter: Damager, subSystem: TurretWeaponSubsystem, isAuto: Boolean) {
		val speed = getProjectileSpeed(starship)

		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			IonTurretProjectile(
				starship,
				subSystem.getName(),
				loc,
				dir,
				speed,
				shooter.color,
				getRange(starship),
				getParticleThickness(starship),
				getExplosionPower(starship),
				getStarshipShieldDamageMultiplier(starship),
				getAreaShieldDamageMultiplier(starship),
				getSound(starship),
				starship.balancing.weapons.ionTurret, // Not used by anything
				shooter
			).fire()
		}
	}
}

object TopIonTurretMultiblock : IonTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomIonTurretMultiblock : IonTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
