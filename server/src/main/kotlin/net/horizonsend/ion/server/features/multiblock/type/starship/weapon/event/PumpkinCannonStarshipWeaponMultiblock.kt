package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.PumpkinCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object PumpkinCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PumpkinCannonWeaponSubsystem>() {
	override val key: String = "pumpkin_cannon"
	fun getAdjustedFace(originalFace: BlockFace): BlockFace {
		return originalFace
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PumpkinCannonWeaponSubsystem {
		val adjustedFace = getAdjustedFace(face)
		return PumpkinCannonWeaponSubsystem(starship, pos, adjustedFace)
	}

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(0).anyCopperVariant()
			}
			y(+0) {
				x(-1).anyCopperVariant()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).anyCopperVariant()
			}
			y(+1) {
				x(0).anyCopperVariant()
			}
		}

		z(+1) {
			y(-1) {
				x(0).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).anyGlass()
			}
			y(+1) {
				x(0).anyGlass()
			}
		}

		z(+2) {
			y(-1) {
				x(0).anyGlass()
			}
			y(+0) {
				x(-1).anyGlass()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).anyGlass()
			}
			y(+1) {
				x(0).anyGlass()
			}
		}

		z(+3) {
			y(-1) {
				x(0).anyCopperVariant()
			}
			y(+0) {
				x(-1).anyCopperVariant()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).anyCopperVariant()
			}
			y(+1) {
				x(0).anyCopperVariant()
			}
		}

		z(+4) {
			y(+0) {
				x(+0).dispenser()
			}
		}
	}
}
