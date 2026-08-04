package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret

import net.horizonsend.ion.server.configuration.starship.AssaultTurretBalancing
import net.horizonsend.ion.server.configuration.starship.AssaultTurretBalancing.AssaultTurretProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.GaussCannonBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AssaultTurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.AssaultTurretProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.GaussCannonProjectile
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.source.StarshipProjectileSource
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs
import org.bukkit.util.Vector
import kotlin.math.sign

sealed class AssaultTurretMultiblock : TurretMultiblock<AssaultTurretProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AssaultTurretWeaponSubsystem {
		return AssaultTurretWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	override val displayName: Component get() = text("Assault Turret (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Rotating weapon system effective against the largest of ships. Manual fire only.")

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<AssaultTurretProjectileBalancing> =
		starship.balancingManager.getWeapon(AssaultTurretWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +3), Vec3i(1, getSign() * 4, +3))

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(getSign() * 3) {
				x(-3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-2).anyConcrete()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).anyConcrete()
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 4) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).ironBlock()
				x(0).terracottaOrDoubleSlab()
				x(1).ironBlock()
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(0) {
			y(getSign() * 3) {
				x(-3).terracottaOrDoubleSlab()
				x(-2).anyConcrete()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).anyConcrete()
				x(3).terracottaOrDoubleSlab()
			}
			y(getSign() * 4) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 2) {
				x(-1).sponge()
				x(1).sponge()
			}
			y(0) {
			}
		}
		z(1) {
			y(getSign() * 3) {
				x(-3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-2).anyConcrete()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).anyConcrete()
				x(3).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 4) {
				x(-2).anyTrapdoor()
				x(-1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(2).anyTrapdoor()
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).anyConcrete()
				x(0).anyConcrete()
				x(1).anyConcrete()
				x(2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
		z(2) {
			y(getSign() * 3) {
				x(-2).ironBlock()
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).ironBlock()
			}
			y(getSign() * 4) {
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).anyTrapdoor()
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
			}
		}
		z(-3) {
			y(getSign() * 3) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
		z(3) {
			y(getSign() * 3) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
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
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<AssaultTurretBalancing.AssaultTurretProjectileBalancing>, AssaultTurretBalancing.AssaultTurretProjectileBalancing>,
		isAuto: Boolean
	) {
		var side: Boolean
		for (point: Vec3i in getAdjustedFirePoints(pos, face)) {
			if (starship.isInternallyObstructed(point, dir)) continue

			side = point.x - pos.x < 0 //if true then left else right

			val loc = point.toLocation(world).toCenterLocation()

			AssaultTurretProjectile(
				StarshipProjectileSource(starship),
				subSystem.getName(),
				loc,
				dir,
				shooter.color,
				shooter,
				subSystem.balancing.projectile,
				side
			).fire()
		}
	}
}

object TopAssaultTurretMultiblock : AssaultTurretMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomAssaultTurretMultiblock : AssaultTurretMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
