package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FireWaveWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object FireWaveWeaponMultiblock : SignlessStarshipWeaponMultiblock<FireWaveWeaponSubsystem>() {
	override val key: String = "fire_wave"
	override val requiredPermission: String = "ioncore.eventweapon"
	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(+0) {
				x(+0).sponge()
				x(+1).ironBlock()
				x(-1).ironBlock()
			}
			y(+1) {
				x(+0).anySlab()
			}
			y(-1) {
				x(+0).anySlab()
			}
		}
		z(+1) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).aluminumBlock()
				x(-1).aluminumBlock()
			}
			y(+1) {
				x(+0).aluminumBlock()
			}
			y(-1) {
				x(+0).aluminumBlock()
			}
		}
		z(+2) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).anyGlass()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+0).anyGlass()
			}
			y(-1) {
				x(+0).anyGlass()
			}
		}
		z(+3) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).anyGlass()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+0).anyGlass()
			}
			y(-1) {
				x(+0).anyGlass()
			}
		}
		z(+4) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).aluminumBlock()
				x(-1).aluminumBlock()
			}
			y(+1) {
				x(+0).aluminumBlock()
			}
			y(-1) {
				x(+0).aluminumBlock()
			}
		}
		z(+5) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).anyGlass()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+0).anyGlass()
			}
			y(-1) {
				x(+0).anyGlass()
			}
		}
		z(+6) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).anyGlass()
				x(-1).anyGlass()
			}
			y(+1) {
				x(+0).anyGlass()
			}
			y(-1) {
				x(+0).anyGlass()
			}
		}
		z(+7) {
			y(+0) {
				x(+0).type(Material.MAGMA_BLOCK)
				x(+1).aluminumBlock()
				x(-1).aluminumBlock()
			}
			y(+1) {
				x(+0).aluminumBlock()
			}
			y(-1) {
				x(+0).aluminumBlock()
			}
		}
		z(+8) {
			y(+0) {
				x(+0).sponge()
				x(+1).ironBlock()
				x(-1).ironBlock()
			}
			y(+1) {
				x(+0).anySlab()
			}
			y(-1) {
				x(+0).anySlab()
			}
		}
		z(+9) {
			y(+0) {
				x(+0).furnace()
			}
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): FireWaveWeaponSubsystem {
		return FireWaveWeaponSubsystem(starship, pos, face)
	}
}
