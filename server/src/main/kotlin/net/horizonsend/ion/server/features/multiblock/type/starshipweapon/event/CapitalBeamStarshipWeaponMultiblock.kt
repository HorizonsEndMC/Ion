package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.CapitalBeamWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace

object CapitalBeamStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<CapitalBeamWeaponSubsystem>() {
	override val requiredPermission: String = "ion.eventship"

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): CapitalBeamWeaponSubsystem {
		return CapitalBeamWeaponSubsystem(starship, pos)
	}

	override fun MultiblockShape.buildStructure() {
		z(-1) {
			y(1) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(2) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(3) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
		}
		z(0) {
			y(1) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(2) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(3) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
		}
		z(+1) {
			y(1) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(2) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
			y(3) {
				x(-1).anyCopperVariant()
				x(0).anyCopperVariant()
				x(+1).anyCopperVariant()
			}
		}

		z(+0) {
			y(0) {
				x(+0).anyCopperVariant()
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
