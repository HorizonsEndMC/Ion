package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.WebifierWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace

object WebifierStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<WebifierWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "webifier"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): WebifierWeaponSubsystem {
		return WebifierWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Stasis Webifier")
	override val description: Component
		get() = text("Can be used to lower a targeted ship's speed by 55%")

	override fun MultiblockShape.buildStructure() {
		z(5) {
			y(0) {
				x(-1).anyWall()
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(1).anyWall()
			}
		}
		z(4) {
			y(0) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
			}
		}
		z(3) {
			y(0) {
				x(-1).ironBlock()
				x(0).chetheriteBlock()
				x(1).ironBlock()
			}
		}
		z(2) {
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).chetheriteBlock()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
		}
		z(1) {
			y(0) {
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).chetheriteBlock()
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(0) {
			y(0) {
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
				x(0).powerInput()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.LEFT))
			}
		}
		z(6) {
			y(0) {
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
	}

}
