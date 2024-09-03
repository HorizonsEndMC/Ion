package net.horizonsend.ion.server.features.multiblock.type.starship.weapon.heavy

import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.starship.weapon.SignlessStarshipWeaponMultiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.subsystem.weapon.secondary.TorpedoWeaponSubsystem
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import org.bukkit.block.BlockFace

object TorpedoStarshipWeaponMultiblock : SignlessStarshipWeaponMultiblock<TorpedoWeaponSubsystem>() {
	override val key: String = "torpedo"
	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TorpedoWeaponSubsystem {
		return TorpedoWeaponSubsystem(starship, pos, face)
	}

	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).sponge()
		at(+0, +0, +1).sponge()
		at(+0, +0, +2).dispenser()
	}
}
