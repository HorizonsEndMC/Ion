package net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

object ArsenalRocketStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ArsenalRocketStarshipWeaponSubsystem>() {
	override fun MultiblockShape.buildStructure() {
		z(-1){
			y(+0){
				x(+1).anyStairs()
				x(0).noteBlock()
				x(-1).anyStairs()
			}
			y(1){
				x(0).sponge()
			}
			y(2){
				x(0).titaniumBlock()
			}
			y(3){
				x(0).titaniumBlock()
			}
			y(4){
				x(0).titaniumBlock()
			}
			y(5){
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}
		z(0){
			y(0){
				x(1).ironBlock()
				x(0).sponge()
				x(-1).ironBlock()
			}
			y(1){
				x(0).sponge()
				x(1).sponge()
				x(-1).sponge()
			}
			y(2){
				x(0).sponge()
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(3){
				x(0).sponge()
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(4){
				x(0).dispenser()
				x(-1).titaniumBlock()
				x(1).titaniumBlock()
			}
			y(5){
				x(-1).ironBlock()
				x(1).ironBlock()
			}
		}
		z(1){
			y(0){
				x(1).anyStairs()
				x(0).ironBlock()
				x(-1).anyStairs()
			}
			y(1){
				x(0).sponge()
			}
			y(2){
				x(0).titaniumBlock()
			}
			y(3){
				x(0).titaniumBlock()
			}
			y(4){
				x(0).titaniumBlock()
			}
			y(5){
				x(1).anyStairs()
				x(0).ironBlock()
				x(-1).anyStairs()
			}
		}

	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): ArsenalRocketStarshipWeaponSubsystem {
	return ArsenalRocketStarshipWeaponSubsystem(starship,pos,face, this)
	}
}
