package net.horizonsend.ion.server.features.multiblock

import net.starlegacy.feature.machine.PowerMachines
import org.bukkit.block.Sign
import org.bukkit.entity.Player

interface PowerStoringMultiblock {
	val maxPower: Int

	fun onTransformSign(player: Player, sign: Sign) {
		PowerMachines.setPower(sign, 0, fast = true)
	}
}
