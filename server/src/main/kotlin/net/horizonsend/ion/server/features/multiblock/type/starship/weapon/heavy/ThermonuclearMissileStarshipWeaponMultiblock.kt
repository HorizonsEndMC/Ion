package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ThermonuclearMissileStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

sealed class ThermonuclearMissileStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ThermonuclearMissileStarshipWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "thermonuclear_missile"
	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace,
	): ThermonuclearMissileStarshipWeaponSubsystem {
		return ThermonuclearMissileStarshipWeaponSubsystem(starship, pos, face, this, upOrDown())
	}

	protected abstract fun upOrDown(): BlockFace
	abstract fun getFirePointOffset(): Vec3i
}

sealed class VerticalThermonuclearMissileWeaponMultiblock : ThermonuclearMissileStarshipWeaponMultiblock() {
	override val displayName: Component get() = text("Thermonuclear missile Launcher (${if (getYFactor() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Launches a massively missile that deals devastating damage.")

	protected abstract fun getYFactor(): Int

	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(0) {
				x(2).sponge()
				x(1).ironBlock()
				x(0).ironBlock()
				x(-1).ironBlock()
				x(-2).sponge()
			}
			y(1) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).ironBlock()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
			y(2) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(4) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(5) {
				x(2).ironBlock()
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).ironBlock()
			}
			y(6) {
				x(2).type(Material.LODESTONE)
				x(1).ironBlock()
				x(0).dispenser()
				x(-1).ironBlock()
				x(-2).type(Material.LODESTONE)
			}
			y(7) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
			}
		}
		z(2) {
			y(0) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(1) {
				x(2).anyTerracotta()
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).anyTerracotta()
			}
			y(2) {
				x(2).anyTerracotta()
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).anyTerracotta()
			}
			y(3) {
				x(2).titaniumBlock()
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).titaniumBlock()
			}
			y(4) {
				x(2).titaniumBlock()
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).titaniumBlock()
			}
			y(5) {
				x(2).titaniumBlock()
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).titaniumBlock()
			}
			y(6) {
				x(2).titaniumBlock()
				x(1).dispenser()
				x(0).dispenser()
				x(-1).dispenser()
				x(-2).titaniumBlock()
			}
			y(7) {
				x(2).ironBlock()
				x(-2).ironBlock()
			}
		}
		z(3) {
			y(0) {
				x(2).sponge()
				x(1).ironBlock()
				x(0).ironBlock()
				x(-1).ironBlock()
				x(-2).sponge()
			}
			y(1) {
				x(2).ironBlock()
				x(1).ironBlock()
				x(0).ironBlock()
				x(-1).ironBlock()
				x(-2).ironBlock()
			}
			y(2) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(4) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(5) {
				x(2).ironBlock()
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
				x(-2).ironBlock()
			}
			y(6) {
				x(2).type(Material.LODESTONE)
				x(1).ironBlock()
				x(0).dispenser()
				x(-1).ironBlock()
				x(-2).type(Material.LODESTONE)
			}
			y(7) {
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.LEFT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(1).ironBlock()
				x(-1).ironBlock()
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.RIGHT,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(3) {
				x(1).titaniumBlock()
				x(0).ironBlock()
				x(-1).titaniumBlock()
			}
		}
		z(0) {
			y(0) {
				x(1).sponge()
				x(0).powerInput()
				x(-1).sponge()
			}
			y(1) {
				x(1).ironBlock()
				x(0).anyGlass()
				x(-1).ironBlock()
			}
			y(2) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).anyTerracotta()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(4) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).titaniumBlock()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(5) {
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
			}
			y(6) {
				x(1).type(Material.LODESTONE)
				x(0).titaniumBlock()
				x(-1).type(Material.LODESTONE)
			}
			y(7) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).ironBlock()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.FORWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(3) {
				x(0).titaniumBlock()
			}
		}
		z(4) {
			y(0) {
				x(1).sponge()
				x(0).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).sponge()
			}
			y(1) {
				x(1).ironBlock()
				x(0).anyTerracotta()
				x(-1).ironBlock()
			}
			y(2) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).anyTerracotta()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(4) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).titaniumBlock()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(5) {
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
			}
			y(6) {
				x(1).type(Material.LODESTONE)
				x(0).titaniumBlock()
				x(-1).type(Material.LODESTONE)
			}
			y(7) {
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).ironBlock()
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(3) {
				x(0).titaniumBlock()
			}
		}
	}
}

	object TopThermonuclearMissileWeaponMultiblock : VerticalThermonuclearMissileWeaponMultiblock() {
		override fun getYFactor() = 1
		override fun getFirePointOffset(): Vec3i = Vec3i(+0, 8, +0)
		override fun upOrDown(): BlockFace = BlockFace.UP
	}

	object BottomThermonuclearMissileWeaponMultiblock : VerticalThermonuclearMissileWeaponMultiblock() {
		override fun getYFactor() = -1
		override fun getFirePointOffset(): Vec3i = Vec3i(+0, -8, +0)
		override fun upOrDown(): BlockFace = BlockFace.DOWN
	}
