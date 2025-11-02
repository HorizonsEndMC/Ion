package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.cannon

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.PulseCannonWeaponSubsystem
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.ScramblerWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace
import org.bukkit.Material

object ScramblerStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ScramblerWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "Scrambler"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ScramblerWeaponSubsystem {
		return ScramblerWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Scrambler")
	override val description: Component
		get() = text("A weapon designed to slow down and prevent ships from jumping without significant damage")

	override fun MultiblockShape.buildStructure() {
		z(3) {
			y(0) {
				x(-1).anyWall()
				x(0).ironBlock()
				x(1).anyWall()
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
				x(-1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.RIGHT, RelativeFace.BACKWARD))
				x(0).chetheriteBlock()
				x(1).anyGlassPane(PrepackagedPreset.pane(RelativeFace.FORWARD, RelativeFace.BACKWARD, RelativeFace.LEFT))
			}
		}
		z(0) {
			y(0) {
				x(-1).anyWall()
				x(0).powerInput()
				x(1).anyWall()
			}
		}
		z(5) {
			y(0) {
				x(0).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
			}
		}
		z(4) {
			y(0) {
				x(0).hopper()
			}
		}
	}
}
