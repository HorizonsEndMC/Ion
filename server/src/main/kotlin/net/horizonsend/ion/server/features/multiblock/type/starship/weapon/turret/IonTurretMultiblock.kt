package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.starship.IonTurretBalancing.IonTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.IonTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.IonTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material.END_ROD
import org.bukkit.Material.GRINDSTONE
import org.bukkit.Material.IRON_TRAPDOOR
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.util.Vector

sealed class IonTurretMultiblock : TurretMultiblock<IonTurretProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): IonTurretWeaponSubsystem {
		return IonTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val displayName: Component get() = text("Ion Turret (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system that slows down starships that are cruising and in Direct Control mode. Manual fire only. Consumes ammo.")

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<IonTurretProjectileBalancing> = starship.balancingManager.getSubsystem(IonTurretWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +3), Vec3i(1, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-3) {
			y(getSign() * 3) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(+0).anyConcrete()
				x(+1).anyConcrete()
				x(+2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs()
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
		}
		z(-1) {
			y(getSign() * 2) {
				x(+0).sponge()
			}
			y(getSign() * 3) {
				x(-3).anyStairs()
				x(-2).anyConcrete()
				x(-1).anyConcrete()
				x(+0).anyCopperVariant()
				x(+1).anyConcrete()
				x(+2).anyConcrete()
				x(+3).anyStairs()
			}
			y(getSign() * 4) {
				x(-2).anySlab()
				x(-1).terracottaOrDoubleSlab()
				x(+0).terracottaOrDoubleSlab()
				x(+1).terracottaOrDoubleSlab()
				x(+2).anySlab()
			}
		}
		z(+0) {
			y(getSign() * 2) {
				x(-1).sponge()
				x(+1).sponge()
			}
			y(getSign() * 3) {
				x(-3).terracottaOrDoubleSlab()
				x(-2).anyConcrete()
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyCopperVariant()
				x(+1).terracottaOrDoubleSlab()
				x(+2).anyConcrete()
				x(+3).terracottaOrDoubleSlab()
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
				x(-2).anyConcrete()
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyCopperVariant()
				x(+1).terracottaOrDoubleSlab()
				x(+2).anyConcrete()
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
				x(-1).terracottaOrDoubleSlab()
				x(+0).anyConcrete()
				x(+1).terracottaOrDoubleSlab()
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
				x(+0).terracottaOrDoubleSlab()
				x(+1).anyStairs()
			}
		}
	}

	override fun shoot(
		world: World,
		pos: Vec3i,
		face: BlockFace,
		dir: Vector,
		starship: ActiveStarship,
		shooter: Damager,
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<IonTurretProjectileBalancing>, IonTurretProjectileBalancing>,
		isAuto: Boolean
	) {
		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			IonTurretProjectile(
				StarshipProjectileSource(starship),
				subSystem.getName(),
				loc,
				dir,
				shooter.color,
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
