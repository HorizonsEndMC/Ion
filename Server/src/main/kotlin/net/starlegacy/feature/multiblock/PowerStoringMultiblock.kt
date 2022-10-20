package net.starlegacy.feature.multiblock

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.util.Vec3i
import net.starlegacy.util.add
import net.starlegacy.util.getFacing
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player

abstract class PowerStoringMultiblock : Multiblock() {
	abstract val maxPower: Int

	override fun onTransformSign(player: Player, sign: Sign) {
		PowerMachines.setPower(sign, 0, fast = true)
	}

	open val inputComputerOffset = Vec3i(0, -1, 0)

	fun getNoteblockLocation(sign: Sign): Location {
		val facing = sign.getFacing().oppositeFace
		return sign.location.add(facing.modX, facing.modY, facing.modZ).add(inputComputerOffset)
	}
}
