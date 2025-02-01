package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.ArsenalRocketStarshipWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

sealed class ArsenalRocketStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<ArsenalRocketStarshipWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(
		starship: ActiveStarship,
		pos: Vec3i,
		face: BlockFace,
	): ArsenalRocketStarshipWeaponSubsystem {
		return ArsenalRocketStarshipWeaponSubsystem(starship, pos, face, this, upOrDown())
	}

	protected abstract fun upOrDown(): BlockFace
}

sealed class VerticalArsenalStarshipWeaponMultiblock : ArsenalRocketStarshipWeaponMultiblock() {
	override val displayName: Component get() = text("Arsenal Missile Launcher (${if (getYFactor() == 1) "Top" else "Bottom"})")
	override val description: Component get() = text("Launches missiles that deal enormous damage only to Area Shields and other land-based defenses. Consumes ammo.")

	protected abstract fun getYFactor(): Int

	override fun MultiblockShape.buildStructure() {
		val yFactor = getYFactor()

		y(0) {
			z(+0) {
			 	 x(-1).anyStairs()
			 	 x(0).powerInput()
			 	 x(+1).anyStairs()
			}
			z(1) {
				x(-1).ironBlock()
				x(0).sponge()
				x(+1).ironBlock()
			}
			z(2) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}
		y(1 * yFactor) {
			z(0) {
				x(0).sponge()
			}
			z(1) {
				x(-1).sponge()
				x(0).sponge()
				x(1).sponge()
			}
			z(2) {
				x(0).sponge()
			}
		}
		y(2 * yFactor) {
			z(0) {
				x(0).titaniumBlock()
			}
			z(1) {
				x(-1).titaniumBlock()
				x(0).sponge()
				x(1).titaniumBlock()
			}
			z(2) {
				x(0).titaniumBlock()
			}
		}
		y(3 * yFactor) {
			z(0) {
				x(0).titaniumBlock()
			}
			z(1) {
				x(-1).titaniumBlock()
				x(0).sponge()
				x(1).titaniumBlock()
			}
			z(2) {
				x(0).titaniumBlock()
			}
		}
		y(4 * yFactor) {
			z(0) {
				x(0).titaniumBlock()
			}
			z(1) {
				x(-1).titaniumBlock()
				x(0).dispenser()
				x(1).titaniumBlock()
			}
			z(2) {
				x(0).titaniumBlock()
			}
		}
		y(5 * yFactor) {
			z(0) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
			z(1) {
				x(-1).ironBlock()
				x(1).ironBlock()
			}
			z(2) {
				x(-1).anyStairs()
				x(0).ironBlock()
				x(1).anyStairs()
			}
		}
	}
}

object TopArsenalStarshipWeaponMultiblock : VerticalArsenalStarshipWeaponMultiblock() {
	override fun getYFactor() = 1

	override fun upOrDown(): BlockFace = BlockFace.UP
}

object BottomArsenalStarshipWeaponMultiblock : VerticalArsenalStarshipWeaponMultiblock() {
	override fun getYFactor() = -1

	override fun upOrDown(): BlockFace = BlockFace.DOWN
}
