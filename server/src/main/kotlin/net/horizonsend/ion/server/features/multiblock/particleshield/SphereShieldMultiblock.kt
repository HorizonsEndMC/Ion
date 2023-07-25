package net.horizonsend.ion.server.features.multiblock.particleshield

import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.getSphereBlocks
import org.bukkit.block.Sign

abstract class SphereShieldMultiblock : ShieldMultiblock() {
	abstract val maxRange: Int

	override fun getShieldBlocks(sign: Sign): List<Vec3i> = getSphereBlocks(maxRange)
}
