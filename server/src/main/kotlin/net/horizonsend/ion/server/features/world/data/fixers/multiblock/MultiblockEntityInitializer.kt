package net.horizonsend.ion.server.features.world.data.fixers.multiblock

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.newer.MultiblockEntities
import net.horizonsend.ion.server.features.multiblock.type.starshipweapon.EntityMultiblock
import net.horizonsend.ion.server.features.world.data.SignDataFixer
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.block.Sign

object MultiblockEntityInitializer : SignDataFixer {
	override fun fixSign(sign: Sign) {
		val multiblock = MultiblockAccess.getFast(sign)
		if (multiblock !is EntityMultiblock<*>) return

		val multiblockDirection = sign.getFacing().oppositeFace

		val (x, y, z) = Vec3i(sign.x, sign.y, sign.z).getRelative(multiblockDirection)

		val entityPresent = MultiblockEntities.getMultiblockEntity(sign.world, x, y, z) != null
		if (entityPresent) return

		MultiblockEntities.setMultiblockEntity(sign.world, x, y, z) { manager ->
			multiblock.createEntity(
				manager,
				PersistentMultiblockData(x, y, z, multiblock, multiblockDirection),
				sign.world,
				x,
				y,
				z,
				multiblockDirection
			)
		}
	}

	override val dataVersion: Int = 1
}
