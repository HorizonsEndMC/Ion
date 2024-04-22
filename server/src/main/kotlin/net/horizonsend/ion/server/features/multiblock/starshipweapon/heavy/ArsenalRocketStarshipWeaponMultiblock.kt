package net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

sealed class ArsenalRocketStarshipWeaponMultiblock :
	SignlessStarshipWeaponMultiblock<ArsenalRocketStarshipWeaponSubsystem>() {
	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace
	): ArsenalRocketStarshipWeaponSubsystem {
		return ArsenalRocketStarshipWeaponSubsystem(starship, pos, getAdjustedDirection(), this)
	}

	protected abstract fun getAdjustedDirection(): BlockFace
}

sealed class VerticalArsenalStarshipWeaponMultiblock : ArsenalRocketStarshipWeaponMultiblock() {
	protected abstract fun getYFactor(): Int

	override fun MultiblockShape.buildStructure() {
		val yFactor = getYFactor()

		y(0*yFactor){
			z(+0){
				x(-1).anyStairs()
				x(0).noteBlock()
				x(+1).anyStairs()
			}
			z(1){
				x(-1).ironBlock()
				x(0).sponge()
				x(+1).ironBlock()
			}
			z(2){
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}
		y(1*yFactor){
			z(0){
				x(0).sponge()
			}
			z(1){
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
			}
			z(2){
				x(0).sponge()
			}
		}
		y(2*yFactor){
			z(0){
				x(0).titaniumBlock()
			}
			z(1){
				x(-1).titaniumBlock()
				x(0).sponge()
				x(1).titaniumBlock()
			}
			z(2){
				x(0).titaniumBlock()
			}
		}
		y(3*yFactor){
			z(0){
				x(0).titaniumBlock()
			}
			z(1){
				x(-1).titaniumBlock()
				x(0).sponge()
				x(1).titaniumBlock()
			}
			z(2){
				x(0).titaniumBlock()
			}
		}
		y(4*yFactor){
			z(0){
				x(0).titaniumBlock()
			}
			z(1){
				x(-1).titaniumBlock()
				x(0).dispenser()
				x(1).titaniumBlock()
			}
			z(2){
				x(0).titaniumBlock()
			}
		}
		y(5*yFactor){
			z(0){
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
			z(1){
				x(-1).ironBlock()
				x(1).ironBlock()
			}
			z(2){
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}
	}
}

object TopArsenalStarshipWeaponMultiblock : VerticalArsenalStarshipWeaponMultiblock() {
	override fun getYFactor() = 1

	override fun getAdjustedDirection(): BlockFace = BlockFace.UP
}

object BottomArsenalStarshipWeaponMultiblock : VerticalArsenalStarshipWeaponMultiblock() {
	override fun getYFactor() = -1

	override fun getAdjustedDirection(): BlockFace = BlockFace.DOWN
}
