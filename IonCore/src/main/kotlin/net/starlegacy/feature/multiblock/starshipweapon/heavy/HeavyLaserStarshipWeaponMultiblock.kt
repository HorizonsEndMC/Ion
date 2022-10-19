package net.starlegacy.feature.multiblock.starshipweapon.heavy

import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.starlegacy.util.Vec3i
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
