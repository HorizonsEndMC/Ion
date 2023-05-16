package net.starlegacy.feature.multiblock.particleshield

import net.horizonsend.ion.server.miscellaneous.Vec3i
import net.starlegacy.util.getSphereBlocks
import org.bukkit.block.Sign

abstract class SphereShieldMultiblock : ShieldMultiblock() {
	abstract val maxRange: Int

	override fun getShieldBlocks(sign: Sign): List<Vec3i> = getSphereBlocks(maxRange)
}
