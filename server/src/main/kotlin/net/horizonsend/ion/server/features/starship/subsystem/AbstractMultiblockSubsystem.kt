package net.horizonsend.ion.server.features.starship.subsystem

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign

abstract class AbstractMultiblockSubsystem<T>(
	starship: ActiveStarship,
	sign: Sign,
	val multiblock: T
) : StarshipSubsystem(starship, Vec3i(sign.location)), DirectionalSubsystem where T : Multiblock {
	override var face: BlockFace = sign.getFacing().oppositeFace

	override fun isIntact(): Boolean {
		val block = starship.world.getBlockAtKey(pos.toBlockKey())

		if (!block.type.isWallSign) {
			return false
		}

		val sign = block.getState(false) as Sign

		return multiblock.signMatchesStructure(sign)
	}
}
