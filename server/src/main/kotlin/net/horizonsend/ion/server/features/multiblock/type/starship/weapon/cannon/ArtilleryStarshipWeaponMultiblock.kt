package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ArtilleryWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PlasmaCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace

object ArtilleryStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ArtilleryWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "artillery"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ArtilleryWeaponSubsystem {
		return ArtilleryWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Artillery")
	override val description: Component
		get() = text("A high-power projectile that deals high alpha with low cooldown")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.LEFT, RelativeFace.FORWARD))
				x(0).ironBlock()
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT))
			}
		}
		z(1) {
			y(0) {
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).titaniumBlock()
				x(-1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(2) {
			y(0) {
				x(1).ironBlock()
				x(0).titaniumBlock()
				x(-1).ironBlock()
			}
		}
		z(3) {
			y(0) {
				x(1).anyWall()
				x(0).ironBlock()
				x(-1).anyWall()
			}
		}
		z(4) {
			y(0) {
				x(1).anyWall()
				x(-1).anyWall()
			}
		}
		z(5) {
			y(0) {
				x(1).anyWall()
				x(-1).anyWall()
			}
		}
		z(6) {
			y(0) {
				x(1).anyWall()
				x(-1).anyWall()
			}
		}
	}
}
