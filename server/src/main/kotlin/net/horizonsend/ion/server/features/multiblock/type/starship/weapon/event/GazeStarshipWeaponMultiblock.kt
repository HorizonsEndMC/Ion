package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.AbyssalGazeSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object GazeStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AbyssalGazeSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AbyssalGazeSubsystem {
		return AbyssalGazeSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		repeat(+3) { z ->
			z(z) {
				y(+0) {
					x(+0).type(Material.EMERALD_BLOCK)
				}
			}
		}

		z(+3) {
			y(+0) {
				x(+0).furnace()
			}
		}
	}
}
