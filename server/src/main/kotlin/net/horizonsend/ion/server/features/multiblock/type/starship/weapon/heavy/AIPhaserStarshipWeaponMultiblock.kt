package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AIPhaserWeaponSystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object AIPhaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AIPhaserWeaponSystem>() {
	override val key: String = "phaser"
	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(+0..+4 step 2) {
				x(+0).anyCopperVariant()
			}
			z(+1..+3 step 2) {
				x(+0).anyFroglight()
			}
			z(+5) { x(+0).anyDoubleSlab() }
			z(+6) { x(+0).hopper() }
			z(+7) { x(+0).type(Material.GRINDSTONE) }
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AIPhaserWeaponSystem {
		return AIPhaserWeaponSystem(starship, pos, face)
	}
}

