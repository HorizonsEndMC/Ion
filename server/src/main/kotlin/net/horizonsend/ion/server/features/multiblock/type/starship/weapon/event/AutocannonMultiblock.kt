package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.configuration.starship.AutocannonBalancing.AutocannonProjectileBalancing
import net.horizonsend.ion.server.configuration.starship.StarshipWeaponBalancing
import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys.KOTH_BLOCK
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.turret.TurretMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.AutocannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

sealed class AutocannonMultiblock : TurretMultiblock<AutocannonProjectileBalancing>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AutocannonWeaponSubsystem {
		return AutocannonWeaponSubsystem(starship, pos, getFacing(pos, starship), this)
	}

	override val displayName: Component get() = text("Autocannon (${if (getSign() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Fast Firing Light-weapon good against small and medium targets Manual fire only.")

	protected abstract fun getSign(): Int

	override fun getBalancing(starship: ActiveStarship): StarshipWeaponBalancing<AutocannonProjectileBalancing> = starship.balancingManager.getWeapon(AutocannonWeaponSubsystem::class)

	override fun buildFirePointOffsets(): List<Vec3i> =
		listOf(Vec3i(-1, getSign() * 4, +2), Vec3i(1, getSign() * 4, +2))

	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(3) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(4) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(0).ironBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
			}
			y(5) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).kothBlock()
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}

			y(2) {
				x(0).sponge()
			}
		}
		z(0) {
			y(3) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).terracottaOrDoubleSlab()
				x(0).anyConcrete()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(2) {
				x(-1).sponge()
				x(1).sponge()
			}
			y(4) {
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).terracottaOrDoubleSlab()
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
			y(5) {
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).terracottaOrDoubleSlab()
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
			y(0) {
			}
		}
		z(-1) {
			y(3) {
				x(-2).anyStairs(PrepackagedPreset.stairs(RelativeFace.RIGHT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(-1).terracottaOrDoubleSlab()
				x(0).terracottaOrDoubleSlab()
				x(1).terracottaOrDoubleSlab()
				x(2).anyStairs(PrepackagedPreset.stairs(RelativeFace.LEFT, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
			y(4) {
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.TOP, shape = Stairs.Shape.STRAIGHT))
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
			y(5) {
				x(-1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
			y(2) {
				x(0).sponge()
			}
		}
		z(2) {
			y(3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.BACKWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
		}
		z(-2) {
			y(3) {
				x(-1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(0).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
				x(1).anyStairs(PrepackagedPreset.stairs(RelativeFace.FORWARD, Bisected.Half.BOTTOM, shape = Stairs.Shape.STRAIGHT))
			}
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
