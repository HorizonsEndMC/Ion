package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.PhaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.BlockFace

object PhaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PhaserWeaponSubsystem>(), DisplayNameMultilblock {

	override val displayName: Component get() = text("Phaser")
	override val description: Component get() = text("Deals heavy shield damage after a warm-up period. Consumes ammo.")

	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(+0..+4 step 2) {
				x(-1).anyWall()
				x(+0).anyCopperVariant()
				x(+1).anyWall()
			}
			z(+1..+3 step 2) {
				x(-1).anyGlassPane()
				x(+0).type(Material.BELL)
				x(+1).anyGlassPane()
			}
			z(+5) { x(+0).anyDoubleSlab() }
			z(+6) { x(+0).hopper() }
			z(+7) { x(+0).grindstone() }
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PhaserWeaponSubsystem {
		return PhaserWeaponSubsystem(starship, pos, face)
	}
}
