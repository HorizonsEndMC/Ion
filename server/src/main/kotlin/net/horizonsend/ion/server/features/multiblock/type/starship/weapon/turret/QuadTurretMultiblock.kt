package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.StarshipWeapons
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.QuadTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.QuadTurretProjectile
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

sealed class QuadTurretMultiblock : TurretMultiblock() {
	override val requiredPermission = "ion.multiblock.quadturret"

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TurretWeaponSubsystem {
		return QuadTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val displayName: Component get() = text("Quad Turret (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system effective against the largest targets. Manual fire only. Consumes ammo.")

	override fun getBalancing(starship: ActiveStarship): StarshipWeapons.StarshipWeapon = starship.balancing.weapons.quadTurret

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-2, getSign() * 4, +3), Vec3i(-1, getSign() * 4, +4), Vec3i(1, getSign() * 4, +4), Vec3i(2, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-4) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
		}
		z(-3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(+1).anyConcrete()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs()
				x(0).anySlab()
				x(+1).anyStairs()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).anyConcrete()
				x(-1).anyConcrete()
				x(+0).anyConcrete()
				x(+1).anyConcrete()
				x(+2).anyConcrete()
				x(+3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).anyStairs()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyStairs()
				x(+1).terracottaOrDoubleSlab()
				x(+2).anyStairs()
			}
		}
		z(-1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-4).anyStairs()
				x(-3).anyConcrete()
				x(-2).anyConcrete()
				x(-1).anyConcrete()
				x(+0).anyConcrete()
				x(+1).anyConcrete()
				x(+2).anyConcrete()
				x(+3).anyConcrete()
				x(+4).anyStairs()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(+0).terracottaOrDoubleSlab()
				x(+1).terracottaOrDoubleSlab()
				x(+2).terracottaOrDoubleSlab()
				x(+3).anySlab()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-4).terracottaOrDoubleSlab()
				x(-3).anyConcrete()
				x(-2).terracottaOrDoubleSlab()
				x(-1).anyConcrete()
				x(+0).anyConcrete()
				x(+1).anyConcrete()
				x(+2).terracottaOrDoubleSlab()
				x(+3).anyConcrete()
				x(+4).terracottaOrDoubleSlab()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).type(GRINDSTONE)
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyStairs()
				x(+1).terracottaOrDoubleSlab()
				x(+2).type(GRINDSTONE)
				x(+3).anySlab()
			}
		}
		z(+1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-4).anyStairs()
				x(-3).anyConcrete()
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyConcrete()
				x(+1).terracottaOrDoubleSlab()
				x(+2).terracottaOrDoubleSlab()
				x(+3).anyConcrete()
				x(+4).anyStairs()
			}
			y(getSign() * 4) {
				x(-3).anySlab()
				x(-2).endRod()
				x(-1).type(GRINDSTONE)
				x(+0).anySlab()
				x(+1).type(GRINDSTONE)
				x(+2).endRod()
				x(+3).anySlab()
			}
		}
		z(+2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyConcrete()
				x(+1).terracottaOrDoubleSlab()
				x(+2).terracottaOrDoubleSlab()
				x(+3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).endRod()
				x(-1).endRod()
				x(0).type(IRON_TRAPDOOR)
				x(+1).endRod()
				x(+2).endRod()
			}
		}
		z(+3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(+1).terracottaOrDoubleSlab()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).endRod()
				x(0).type(IRON_TRAPDOOR)
				x(1).endRod()
			}
		}
		z(+4) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
		}
	}

	override fun shoot(world: World, pos: Vec3i, face: BlockFace, dir: Vector, starship: ActiveStarship, shooter: Damager, subSystem: TurretWeaponSubsystem, isAuto: Boolean) {
		val speed = getProjectileSpeed(starship)

		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			QuadTurretProjectile(
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
				starship.balancing.weapons.quadTurret, // Not used by anything
				starship.balancing.weapons.quadTurret.soundFireNear,
				starship.balancing.weapons.quadTurret.soundFireFar,
				shooter
			).fire()
		}
	}
}

object TopQuadTurretMultiblock : QuadTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomQuadTurretMultiblock : QuadTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
