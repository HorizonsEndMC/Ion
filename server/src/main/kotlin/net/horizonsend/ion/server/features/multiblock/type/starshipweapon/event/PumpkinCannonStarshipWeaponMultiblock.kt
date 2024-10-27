package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.PumpkinCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object PumpkinCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PumpkinCannonWeaponSubsystem>() {
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
				x(0).copperBlock()
			}
			y(+0) {
				x(-1).copperBlock()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).copperBlock()
			}
			y(+1) {
				x(0).copperBlock()
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
				x(0).copperBlock()
			}
			y(+0) {
				x(-1).copperBlock()
				x(+0).type(Material.SHROOMLIGHT)
				x(+1).copperBlock()
			}
			y(+1) {
				x(0).copperBlock()
			}
		}

		z(+4) {
			y(+0) {
				x(+0).dispenser()
			}
		}
	}
}
