package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.HeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.block.BlockFace

object HeavyLaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<HeavyLaserWeaponSubsystem>(), DisplayNameMultilblock {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): HeavyLaserWeaponSubsystem {
		return HeavyLaserWeaponSubsystem(starship, pos, face)
	}

	override val displayName: Component
		get() = text("Heavy Laser")
	override val description: Component
		get() = text("A heavy weapon with a homing projectile. Slows down small ships on impact if the firing ship is also small.")

	override fun MultiblockShape.buildStructure() {
		repeat(+7) { z ->
			z(z) {
				y(-1) {
					x(+0).anyGlass()
				}
				y(+0) {
					x(-1).anyGlass()
					x(+0).redstoneBlock()
					x(+1).anyGlass()
				}
				y(+1) {
					x(+0).anyGlass()
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
