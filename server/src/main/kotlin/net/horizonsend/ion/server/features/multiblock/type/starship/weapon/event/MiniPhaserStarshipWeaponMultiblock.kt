package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.event

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.event.MiniPhaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object MiniPhaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<MiniPhaserWeaponSubsystem>() {
	override val key: String = "mini_phaser"
	override fun MultiblockShape.buildStructure() {
		y(+0) {
			z(+0) { x(+0).type(Material.IRON_BLOCK) }
			z(+1) { x(+0).type(Material.SPONGE) }
			z(+2) { x(+0).type(Material.IRON_BLOCK) }
			z(+3) { x(+0).type(Material.HOPPER) }
			z(+4) { x(+0).grindstone() }
			z(+5) { x(+0).type(Material.END_ROD) }
		}
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): MiniPhaserWeaponSubsystem {
		return MiniPhaserWeaponSubsystem(starship, pos, face)
	}
}
