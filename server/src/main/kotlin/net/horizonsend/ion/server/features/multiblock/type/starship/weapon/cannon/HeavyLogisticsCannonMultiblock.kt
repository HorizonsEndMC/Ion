package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.HeavyLogisticsCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object HeavyLogisticsCannonMultiblock : SignlessStarshipWeaponMultiblock<HeavyLogisticsCannonWeaponSubsystem>(), DisplayNameMultilblock {

	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace
	): HeavyLogisticsCannonWeaponSubsystem { return HeavyLogisticsCannonWeaponSubsystem(starship, pos, face) }


	override val displayName: Component get() = text("Heavy Logistics cannon")
	override val description: Component get() = text("Weapon system that heals other starships.")
	override val key: String = "heavy_logistics_cannon"


	override fun MultiblockShape.buildStructure() {
		z(1) {
			y(-1) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(-1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(0).ironBlock()
				x(1).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.TOP,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(0) {
				x(-2).ironBlock()
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
				x(2).ironBlock()
			}
			y(1) {
				x(-2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
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
				x(2).anyStairs(
					PrepackagedPreset.stairs(
						RelativeFace.BACKWARD,
						Bisected.Half.BOTTOM,
						shape = Stairs.Shape.STRAIGHT
					)
				)
			}
			y(-2) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
			y(2) {
				x(-1).ironBlock()
				x(0).ironBlock()
				x(1).ironBlock()
			}
		}
		z(2) {
			y(0) {
				x(-2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(0).type(Material.LODESTONE)
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD, RelativeFace.LEFT))
				x(2).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(-2) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(-1) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
			y(1) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
			y(2) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
		}
		z(0) {
			y(0) {
				x(-1).anyGlass()
				x(0).emeraldBlock()
				x(1).anyGlass()
			}
		}
	}
}
