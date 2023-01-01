package net.starlegacy.feature.multiblock.starshipweapon.cannon

import net.starlegacy.feature.multiblock.LegacyMultiblockShape
import net.starlegacy.feature.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.primary.LaserCannonWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

object LaserCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<LaserCannonWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): LaserCannonWeaponSubsystem {
		return LaserCannonWeaponSubsystem(starship, pos, face)
	}

	override fun LegacyMultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).pistonBase()
	}
}
