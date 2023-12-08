package net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AIHeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object AIHeavyLaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AIHeavyLaserWeaponSubsystem>() {
	override val requiredPermission: String = "ion.weapon.ai"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AIHeavyLaserWeaponSubsystem {
		return AIHeavyLaserWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		z(+6) {
			y(+0) {
				x(+0).pistonBase()
			}
		}
		z(+5) {
			y(+0) {
				x(+0).type(Material.GRINDSTONE)
			}
		}
		z(+4) {
			y(+0) {
				x(+0).furnace()
			}
		}
		z(+3) {
			y(+0) {
				x(+0).furnace()
			}
		}
		z(+2) {
			y(+0) {
				x(+0).thrusterBlock()
			}
		}
		z(+1) {
			y(+0) {
				x(+0).thrusterBlock()
			}
		}
		z(+0) {
			y(+0) {
				x(+0).thrusterBlock()
			}
		}
	}
}
