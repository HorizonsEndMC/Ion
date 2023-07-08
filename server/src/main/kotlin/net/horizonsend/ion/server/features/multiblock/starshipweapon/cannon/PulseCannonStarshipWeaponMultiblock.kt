package net.horizonsend.ion.server.features.multiblock.starshipweapon.cannon

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.feature.starship.subsystem.weapon.primary.PulseCannonWeaponSubsystem
import net.starlegacy.util.Vec3i
import org.bukkit.block.BlockFace

object PulseCannonStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<PulseCannonWeaponSubsystem>() {
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): PulseCannonWeaponSubsystem {
		return PulseCannonWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).stainedGlass()
	}
}
