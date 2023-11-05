package net.horizonsend.ion.server.features.multiblock.starshipweapon.event

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.CapitalBeamWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

object CapitalBeamStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<CapitalBeamWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CapitalBeamWeaponSubsystem {
		return CapitalBeamWeaponSubsystem(starship, pos)
	}

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(1) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(2) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(3) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
		}
		z(0) {
			y(1) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(2) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(3) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
		}
		z(+1) {
			y(1) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(2) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
			y(3) {
				x(-1).copperBlock()
				x(0).copperBlock()
				x(+1).copperBlock()
			}
		}

		z(+0) {
			y(0) {
				x(+0).copperBlock()
			}
			y(+4) {
				x(+0).anyWall()
			}
			y(+5) {
				x(+0).anyWall()
			}
			y(+6) {
				x(+0).lodestone()
			}
		}
	}
}
