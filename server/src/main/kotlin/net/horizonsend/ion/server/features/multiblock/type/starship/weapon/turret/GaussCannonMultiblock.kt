package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing
import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing.GaussCannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.GaussCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.GaussCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Slab
import org.bukkit.block.data.type.Stairs
import org.bukkit.util.Vector

sealed class GaussCannonMultiblock : TurretMultiblock<GaussCannonProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): GaussCannonWeaponSubsystem {
		return GaussCannonWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	override val displayName: Component get() = text("Gauss Cannon (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system effective against medium and large targets. Manual fire only.")

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<GaussCannonProjectileBalancing> = starship.balancingManager.getWeapon(GaussCannonWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(0, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() * 3) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(0) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(0).terracottaOrDoubleSlab()
				x(1).anyConcrete()
				x(2).ironBlock()
			}
			y(getSign() * 4) {
				x(-2).anyTrapdoor()
				x(-1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anySlab(PrepackagedPreset.slab(Slab.Type.BOTTOM))
				x(2).anyTrapdoor()
			}
			y(getSign() * 2) {
				x(-1).sponge()
				x(1).sponge()
			}
		}
		z(1) {
			y(getSign() * 3) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).anyConcrete()
				x(0).terracottaOrDoubleSlab()
				x(1).anyConcrete()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(-1).anyTrapdoor()
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anyTrapdoor()
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(0).ironBlock()
			}
		}
		z(2) {
			y(getSign() * 3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(getSign() * 4) {
				x(0).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
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
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<GaussCannonBalancing.GaussCannonProjectileBalancing>, GaussCannonBalancing.GaussCannonProjectileBalancing>,
		isAuto: Boolean
	) {
		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			GaussCannonProjectile(
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

object TopGaussCannonMultiblock : GaussCannonMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomGaussCannonMultiblock : GaussCannonMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
