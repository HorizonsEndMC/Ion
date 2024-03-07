package net.horizonsend.ion.server.features.multiblock.type.particleshield

import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getSphereBlocks
import org.bukkit.block.Sign

abstract class SphereShieldMultiblock : ShieldMultiblock() {
	abstract val maxRange: Int

	override fun getShieldBlocks(sign: Sign): List<Vec3i> = getSphereBlocks(maxRange)
}
