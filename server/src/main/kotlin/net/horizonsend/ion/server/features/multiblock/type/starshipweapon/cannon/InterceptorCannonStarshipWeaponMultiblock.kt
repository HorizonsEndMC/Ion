package net.horizonsend.ion.server.features.multiblock.type.starshipweapon.cannon

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.primary.InterceptorCannonWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import org.bukkit.block.BlockFace

object InterceptorCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<InterceptorCannonWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): InterceptorCannonWeaponSubsystem {
		return InterceptorCannonWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).ironBlock()
		at(+0, +0, +1).pistonBase()
	}
}
