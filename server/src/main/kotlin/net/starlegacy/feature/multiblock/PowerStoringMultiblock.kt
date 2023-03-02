package net.starlegacy.feature.multiblock

import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.util.Vec3i
import net.starlegacy.util.add
import net.starlegacy.util.getFacing
import org.bukkit.Location
import org.bukkit.block.Sign
import org.bukkit.entity.Player

interface PowerStoringMultiblock {
	val maxPower: Int

	fun onTransformSign(player: Player, sign: Sign) {
		PowerMachines.setPower(sign, 0, fast = true)
	}

	val inputComputerOffset: Vec3i

	fun getNoteblockLocation(sign: Sign): Location {
		val facing = sign.getFacing().oppositeFace
		return sign.location.add(facing.modX, facing.modY, facing.modZ).add(inputComputerOffset)
	}
}
