package net.horizonsend.ion.server.features.multiblock.starshipweapon.heavy

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.AIHeavyLaserWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object AIHeavyLaserStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<AIHeavyLaserWeaponSubsystem>() {
	override val requiredPermission: String = "ion.weapon.ai"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): AIHeavyLaserWeaponSubsystem {
		return AIHeavyLaserWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).thrusterBlock()
		at(+0, +0, +1).thrusterBlock()
		at(+0, +0, +2).thrusterBlock()
		at(+0, +0, +3).type(Material.DROPPER)
		at(+0, +0, +4).type(Material.DROPPER)
		at(+0, +0, +5).type(Material.GRINDSTONE)
		at(+0, +0, +6).pistonBase()
	}
}
