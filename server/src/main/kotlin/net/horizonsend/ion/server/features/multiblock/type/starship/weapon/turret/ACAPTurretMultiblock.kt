package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.starship.ACAPTurretBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ACAPTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.ACAPTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.LaserProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Color
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.util.Vector

sealed class ACAPTurretMultiblock : TurretMultiblock<ACAPTurretBalancing.ACAPTurretProjectileBalancing>() {
	override val requiredPermission = "ion.multiblock.acapturret"

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ACAPTurretWeaponSubsystem {
		return ACAPTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	protected abstract fun getSign(): Int

	override val displayName: Component get() = text("ACAP Turret (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system extremely effective against the largest targets. Manual fire only. Consumes ammo.")

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<ACAPTurretBalancing.ACAPTurretProjectileBalancing> = starship.balancingManager.getWeapon(ACAPTurretWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(0, getSign() * 4, +4), Vec3i(-2, getSign() * 4, +3), Vec3i(+2, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() * 3) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).anyConcrete()
				x(-2).terracottaOrDoubleSlab()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).terracottaOrDoubleSlab()
				x(3).anyConcrete()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(-3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(3).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(0) {
			y(getSign() * 3) {
				x(-4).terracottaOrDoubleSlab()
				x(-3).anyConcrete()
				x(-2).terracottaOrDoubleSlab()
				x(-1).anyConcrete()
				x(0).terracottaOrDoubleSlab()
				x(1).anyConcrete()
				x(2).terracottaOrDoubleSlab()
				x(3).anyConcrete()
				x(4).terracottaOrDoubleSlab()
			}
			y(getSign() * 4) {
				x(-3).anyTrapdoor()
				x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(3).anyTrapdoor()
			}
			y(getSign() * 2) {
				x(-1).sponge()
				x(1).sponge()
			}
		}
		z(1) {
			y(getSign() * 3) {
				x(-4).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-3).anyConcrete()
				x(-2).terracottaOrDoubleSlab()
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).terracottaOrDoubleSlab()
				x(3).anyConcrete()
				x(4).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(-3).anyTrapdoor()
				x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(3).anyTrapdoor()
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).anyConcrete()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).anyConcrete()
				x(3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).ironBlock()
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).ironBlock()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(2) {
			y(getSign() * 3) {
				x(-3).ironBlock()
				x(-2).terracottaOrDoubleSlab()
				x(-1).anyConcrete()
				x(0).terracottaOrDoubleSlab()
				x(1).anyConcrete()
				x(2).terracottaOrDoubleSlab()
				x(3).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(-1).anyTrapdoor()
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anyTrapdoor()
				x(2).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(-3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(3) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(0).terracottaOrDoubleSlab()
				x(1).anyConcrete()
				x(2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyTrapdoor()
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anyTrapdoor()
			}
		}
		z(-4) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).terracottaOrDoubleSlab()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(4) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).terracottaOrDoubleSlab()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
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
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<ACAPTurretBalancing.ACAPTurretProjectileBalancing>, ACAPTurretBalancing.ACAPTurretProjectileBalancing>,
		isAuto: Boolean
	) {
		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			ACAPTurretProjectile(
				StarshipProjectileSource(starship),
				subSystem.getName(),
				loc,
				dir,
				shooter.color,
				shooter,
				subSystem.balancing.projectile
			).fire()
		}
	}
}

object TopACAPTurretMultiblock : ACAPTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomACAPTurretMultiblock : ACAPTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
