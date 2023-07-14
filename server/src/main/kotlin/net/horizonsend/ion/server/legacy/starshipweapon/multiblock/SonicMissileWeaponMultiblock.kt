package net.horizonsend.ion.server.legacy.starshipweapon.multiblock

import net.horizonsend.ion.server.legacy.starshipweapon.secondary.SonicMissileWeaponSubsystem
import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object SonicMissileWeaponMultiblock : SignlessStarshipWeaponMultiblock<SonicMissileWeaponSubsystem>() {
	override fun LegacyMultiblockShape.buildStructure() {
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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
				x(+0).type(Material.SCULK)
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

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): SonicMissileWeaponSubsystem {
		return SonicMissileWeaponSubsystem(starship, pos, face)
	}
}
