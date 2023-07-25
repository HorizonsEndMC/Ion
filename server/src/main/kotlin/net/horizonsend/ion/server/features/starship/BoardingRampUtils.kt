package net.horizonsend.ion.server.features.starship

import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import org.bukkit.Bukkit
import org.bukkit.Material
import org.bukkit.Sound
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.block.data.BlockData
import org.bukkit.entity.Player

// probably considered GPL licensed since it's still part of the original plugin, should be replaced later
object BoardingRampUtils {
	fun openRamp(sign: Sign) {
		val base = sign.block.getRelative(BlockFace.DOWN)
		val ramp = base.getRelative(sign.getFacing())
		val air = ramp.getRelative(BlockFace.DOWN)

		if (air.type != Material.AIR || !FLYABLE_BLOCKS.contains(ramp.type) || !FLYABLE_BLOCKS.contains(base.type)) {
			return
		}

		sign.setLine(1, BoardingRamps.SECOND_LINE_OPEN)
		sign.setLine(2, base.blockData.asString)
		sign.setLine(3, ramp.blockData.asString)
		sign.update()

		air.setType(ramp.type, false)
		ramp.setType(Material.AIR, false)
		base.setType(Material.AIR, false)
	}

	fun closeRamp(sign: Sign, interactor: Player): Boolean {
		val facing = sign.getFacing()
		val sBlock = sign.block
		val changeblock = sBlock.getRelative(BlockFace.DOWN)
		val movingstepsup = changeblock.getRelative(facing)
		val movingstepsdown = movingstepsup.getRelative(BlockFace.DOWN)
		val changeBlockData: BlockData
		val movingBlockData: BlockData
		try {
			changeBlockData = checkNotNull(Bukkit.createBlockData(sign.getLine(2)))
			movingBlockData = checkNotNull(Bukkit.createBlockData(sign.getLine(3)))
		} catch (e: Exception) {
			interactor.sendMessage("Your boarding ramp sign is invalid. Try replacing it.")
			return false
		}
		if (movingstepsdown.type === movingBlockData.material) {
			if (changeblock.type == Material.AIR) {
				changeblock.setBlockData(changeBlockData, true)
			}
			if (movingstepsup.type == Material.AIR) {
				movingstepsup.setBlockData(movingBlockData, true)
				movingstepsdown.type = Material.AIR
			}
			shutSign(sign)
			return true
		}
		return false
	}

	private fun shutSign(sign: Sign) {
		sign.setLine(0, BoardingRamps.FIRST_LINE)
		sign.setLine(1, BoardingRamps.SECOND_LINE_SHUT)
		sign.setLine(2, "")
		sign.setLine(3, "")
		sign.update()
		sign.block.world.playSound(
			sign.block.location, Sound.BLOCK_PISTON_CONTRACT,
			2.0f, 1.0f
		)
	}
}
