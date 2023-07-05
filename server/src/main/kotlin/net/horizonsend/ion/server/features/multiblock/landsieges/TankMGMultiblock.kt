package net.horizonsend.ion.server.features.multiblock.landsieges

import net.horizonsend.ion.server.features.multiblock.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.landsieges.tank.TankCannonSubsystem
import net.horizonsend.ion.server.features.multiblock.landsieges.tank.TankMGSubsystem
import net.horizonsend.ion.server.features.multiblock.starshipweapon.SignlessStarshipWeaponMultiblock
import net.starlegacy.feature.starship.active.ActiveStarship
import net.starlegacy.util.Vec3i
import org.bukkit.Material
import org.bukkit.block.BlockFace

object TankMGMultiblock: SignlessStarshipWeaponMultiblock<TankMGSubsystem>() {
	override fun MultiblockShape.buildStructure() {
		at(+0, +0, +0).anyStairs()
		at(+0, +0, +1).grindstone()
		at(+0, +0, +2).endRod()
	}

	override fun createSubsystem(starship: ActiveStarship, pos: Vec3i, face: BlockFace): TankMGSubsystem {
		return TankMGSubsystem(starship, pos, face)
	}
}
