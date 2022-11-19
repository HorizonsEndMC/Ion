package net.horizonsend.ion.server.networks.nodes

import com.fasterxml.jackson.annotation.JsonIgnore
import net.horizonsend.ion.server.networks.connections.AbstractConnection
import net.horizonsend.ion.server.networks.connections.WiredConnection
import net.horizonsend.ion.server.utilities.Position
import net.minecraft.core.BlockPos
import net.minecraft.tags.BlockTags
import net.minecraft.world.level.block.Blocks
import net.starlegacy.feature.machine.PowerMachines
import net.starlegacy.feature.multiblock.Multiblocks
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import org.bukkit.Bukkit
import org.bukkit.block.Sign
import org.bukkit.craftbukkit.v1_19_R1.CraftWorld
import org.bukkit.craftbukkit.v1_19_R1.block.CraftBlockStates
import kotlin.math.min

class ComputerNode : AbstractNode() {
	override fun canStepFrom(lastNode: AbstractNode, lastConnection: AbstractConnection): Boolean {
		return lastConnection is WiredConnection || lastNode !is ExtractorNode
	}

	private var lastFoundOffset: Position<Int>? = null
	private var multiblock: PowerStoringMultiblock? = null
	private var sign: Sign? = null

	private var originalPowerValue = 0
	private var _powerValue = 0

	private var lastLoadedTick = Int.MIN_VALUE

	fun consume(availablePower: Int): Int {
		powerValue // Dumb solution to ensure multiblock is loaded if it exists

		// Check for valid multiblock
		if (multiblock == null) return 0

		val consumedPower = min(availablePower, multiblock!!.maxPower - powerValue)
		powerValue += consumedPower

		return consumedPower
	}

	var powerValue: Int
		@JsonIgnore get() {
			if (lastLoadedTick == Bukkit.getCurrentTick()) return _powerValue
			lastLoadedTick = Bukkit.getCurrentTick()

			multiblock = null
			_powerValue = 0
			sign = null

			if (isLoaded) {
				fun loadFromOffset(offset: Position<Int>): Boolean {
					val signX = position.x + offset.x
					val signY = position.y + offset.y
					val signZ = position.z + offset.z

					val blockPos = BlockPos(signX, signY, signZ)

					val serverLevel = (ionChunk.ionWorld.serverLevel.world as CraftWorld).handle
					val blockState = serverLevel.getBlockStateIfLoaded(blockPos) ?: return false

					if (!blockState.`is`(BlockTags.SIGNS)) return false

					sign = CraftBlockStates.getBlockState(
						ionChunk.ionWorld.serverLevel.world.getBlockAt(signX, signY, signZ),
						false
					) as? Sign ?: return false
					multiblock = Multiblocks[sign!!, false, false] as? PowerStoringMultiblock ?: return false

					_powerValue = PowerMachines.getPower(sign!!)
					return true
				}

				val usedLastOffset = if (lastFoundOffset == null) false else loadFromOffset(lastFoundOffset!!)

				if (!usedLastOffset) for (offset in signOffsets) {
					if (offset == lastFoundOffset) continue
					if (loadFromOffset(offset)) {
						lastFoundOffset = offset
						break
					}
				}
			}

			originalPowerValue = _powerValue

			return _powerValue
		}
		set(value) {
			_powerValue = value
		}

	fun save() {
		if (originalPowerValue == _powerValue) return

		PowerMachines.setPower(sign ?: return, _powerValue)
	}

	companion object : AbstractNodeCompanion<ComputerNode>(Blocks.NOTE_BLOCK) {
		override fun construct(): ComputerNode = ComputerNode()

		private val signOffsets = arrayOf(
			Position( 1, 1,  0), Position(-1, 1,  0), Position(0, 1, -1), Position( 0, 1, 1),
			Position( 1, 0,  0), Position(-1, 0,  0), Position(0, 0, -1), Position( 0, 0, 1), // Power Cells
			Position(-1, 0, -1), Position( 1, 0, -1), Position(1, 0,  1), Position(-1, 0, 1)  // Drills
		)
	}
}