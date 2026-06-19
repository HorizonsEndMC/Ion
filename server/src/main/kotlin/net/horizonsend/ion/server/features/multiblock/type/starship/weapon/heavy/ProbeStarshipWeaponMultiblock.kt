package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.multiblock.util.PrepackagedPreset
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ProbeWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.RelativeFace
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace

object ProbeStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ProbeWeaponSubsystem {
		return ProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Probe Launcher")
	override val description: Component
		get() = text("Fires a scanner probe that detects nearby signatures.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(1).sponge()
				x(0).powerInput()
			}
		}
		z(1) {
			y(0) {
				x(1).grindstone(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.GRINDSTONE.createBlockData()))
				x(0).dispenser()
			}
		}
		z(2) {
			y(0) {
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.FORWARD, example = Material.END_ROD.createBlockData()))
			}
		}
		z(3) {
			y(0) {
				x(1).endRod(PrepackagedPreset.simpleDirectional(RelativeFace.BACKWARD, example = Material.END_ROD.createBlockData()))
			}
		}
	}
}
