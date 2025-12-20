package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing
import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing.AutocannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipTurretWeaponBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys.KOTH_BLOCK
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.damager.Damager
import net.horizonsend.ion.server.features.starship.subsystem.weapon.TurretWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AutocannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.projectile.AutocannonProjectile
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

sealed class AutocannonMultiblock : TurretMultiblock<AutocannonProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AutocannonWeaponSubsystem {
		return AutocannonWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	override val displayName: Component get() = text("Autocannon (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Fast Firing Light-weapon good against small and medium targets Manual fire only.")

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<AutocannonProjectileBalancing> =
		starship.balancingManager.getWeapon(AutocannonWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(
			Vec3i(-1, getSign() * 4, -2),
			Vec3i(1, getSign() * 5, -2),
			Vec3i(-1, getSign() * 4, -2),
			Vec3i(-1, getSign() * 5, -2)
		)

	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(getSign() * 3) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 4) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).ironBlock()
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 5) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).kothBlock()
				x(1).anyStairs(
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
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 2) {
				x(-1).sponge()
				x(1).sponge()
			}
			y(getSign() * 4) {
				x(-1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
			}
			y(getSign() * 5) {
				x(-1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
				x(0).terracottaOrDoubleSlab()
				x(1).grindstone(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.BACKWARD,
						example = Material.GRINDSTONE.createBlockData()
					)
				)
			}
			y(getSign() * 0) {
			}
		}
		z(-1) {
			y(getSign() * 3) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(getSign() * 4) {
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
			}
			y(getSign() * 5) {
				x(-1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).endRod(
					PrepackagedPreset.simpleDirectional(
						RelativeFace.FORWARD,
						example = Material.END_ROD.createBlockData()
					)
				)
			}
			y(getSign() * 2) {
				x(0).sponge()
			}
		}
		z(2) {
			y(getSign() * 3) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
		}
		z(-2) {
			y(getSign() * 3) {
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
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
		subSystem: TurretWeaponSubsystem<out StarshipTurretWeaponBalancing<AutocannonBalancing.AutocannonProjectileBalancing>, AutocannonBalancing.AutocannonProjectileBalancing>,
		isAuto: Boolean
	) {
		for ((index, point) in getAdjustedFirePoints(pos, face).withIndex()) {
			if (starship.isInternallyObstructed(point, dir)) continue

			val loc = point.toLocation(world).toCenterLocation()

			AutocannonProjectile(
				StarshipProjectileSource(starship),
				subSystem.getName(),
				loc,
				dir,
				shooter.color,
				shooter,
				index,
				this
			).fire()
		}
	}
}

object TopAutocannonMultiblock : AutocannonMultiblock() {
	override fun getSign(): Int = 1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, +3, +1)
}

object BottomAutocannonMultiblock : AutocannonMultiblock() {
	override fun getSign(): Int = -1
	override fun getPilotOffset(): Vec3i = Vec3i(+0, -4, +1)
}
