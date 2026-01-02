package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.LightLogisticsCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace
import org.bukkit.block.data.Bisected
import org.bukkit.block.data.type.Stairs

object LightLogisticsCannonMultiblock : SignlessStarshipWeaponMultiblock<LightLogisticsCannonWeaponSubsystem>(), DisplayNameMultilblock {

	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace
	): LightLogisticsCannonWeaponSubsystem {
		return LightLogisticsCannonWeaponSubsystem(starship, pos, face)
	}


	override val displayName: Component get() = text("Light Logistics cannon")
	override val description: Component get() = text("Weapon system that heals other starships.")
	override val key: String = "light_logistics_cannon"

	override fun MultiblockShape.buildStructure() {
		z(2) {
			y(-1) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).type(Material.LODESTONE)
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
			y(1) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.RIGHT))
				x(0).anyWall()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT))
			}
		}
		z(1) {
			y(-1) {
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
			y(0) {
				x(-1).ironBlock()
				x(0).emeraldBlock()
				x(1).ironBlock()
			}
			y(1) {
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
			}
		}
		z(0) {
			y(-1) {
				x(-1).ironBlock()
				x(1).ironBlock()
			}
			y(0) {
				x(-1).ironBlock()
				x(0).emeraldBlock()
				x(1).ironBlock()
			}
			y(1) {
				x(-1).ironBlock()
				x(1).ironBlock()
			}
		}
	}
}

