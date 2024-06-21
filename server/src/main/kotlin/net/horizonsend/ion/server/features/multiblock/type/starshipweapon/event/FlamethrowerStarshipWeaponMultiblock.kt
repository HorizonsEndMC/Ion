package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.FlamethrowerWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object FlamethrowerStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<FlamethrowerWeaponSubsystem>() {
	override val key: String = "flamethrower"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): FlamethrowerWeaponSubsystem {
		return FlamethrowerWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		repeat(+7) { z ->
			z(z) {
				y(+0) {
					x(+0).type(Material.MAGMA_BLOCK)
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
