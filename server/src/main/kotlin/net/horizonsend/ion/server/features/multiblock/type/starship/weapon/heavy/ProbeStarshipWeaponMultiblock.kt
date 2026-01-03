package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ProbeWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object ProbeStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ProbeWeaponSubsystem>(), DisplayNameMultilblock {
	override val key: String = "probe"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ProbeWeaponSubsystem {
		return ProbeWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Probe")
	override val description: Component
		get() = text("Fires off a probe that displays nearby players and their distance.")

	override fun MultiblockShape.buildStructure() {
		z(0) {
			y(0) {
				x(0).powerInput()
				x(+1).anyStairs()
			}
		}
		z(1) {
			y(0) {
				x(0).ironBlock()
				x(+1).sponge()
			}
		}
		z(2) {
			y(0) {
				x(0).ironBlock()
				x(+1).grindstone()
			}
		}
		z(3) {
			y(0) {
				x(0).dispenser()
				x(+1).grindstone(
				)
			}
		}
		z(4) {
			y(0) {
				x(+1).endRod()
			}
		}
		z(5) {
			y(0) {
				x(+1).endRod()
			}
		}
	}
}
