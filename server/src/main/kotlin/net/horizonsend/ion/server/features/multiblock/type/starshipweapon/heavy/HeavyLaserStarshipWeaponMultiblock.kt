package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.heavy

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

object HeavyLaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<HeavyLaserWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): HeavyLaserWeaponSubsystem {
		return HeavyLaserWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		repeat(+7) { z ->
			z(z) {
				y(-1) {
					x(+0).stainedGlass()
				}
				y(+0) {
					x(-1).stainedGlass()
					x(+0).redstoneBlock()
					x(+1).stainedGlass()
				}
				y(+1) {
					x(+0).stainedGlass()
				}
			}
		}

		z(+7) {
			y(+0) {
				x(+0).furnace()
			}
		}
	}
}
