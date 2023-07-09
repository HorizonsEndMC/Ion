package net.starlegacy.feature.multiblock.starshipweapon.heavy

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object PhaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PhaserWeaponSubsystem>() {
	override fun LegacyMultiblockShape.buildStructure() {
		y(+0) {
			z(+0..+4 step 2) {
				x(-1).anyWall()
				x(+0).copperBlock()
				x(+1).anyWall()
			}
			z(+1..+3 step 2) {
				x(-1).anyGlassPane()
				x(+0).type(Material.BELL)
				x(+1).anyGlassPane()
			}
			z(+5) { x(+0).anyDoubleSlab() }
			z(+6) { x(+0).hopper() }
			z(+7) { x(+0).type(Material.GRINDSTONE) }
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PhaserWeaponSubsystem {
		return PhaserWeaponSubsystem(starship, pos, face)
	}
}
