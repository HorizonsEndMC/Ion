package net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.primary.LaserCannonWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

object LaserCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<LaserCannonWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): LaserCannonWeaponSubsystem {
		return LaserCannonWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).pistonBase()
	}
}
