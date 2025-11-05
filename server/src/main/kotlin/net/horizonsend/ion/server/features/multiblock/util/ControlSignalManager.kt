package net.horizonsend.ion.server.features.multiblock.util

import net.horizonsend.ion.server.core.registration.keys.CustomBlockKeys
import net.horizonsend.ion.server.core.registration.registries.CustomBlockRegistry.Companion.customBlock
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.getBlockDataSafe
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i

class ControlSignalManager private constructor(val multiblockEntity: MultiblockEntity) {
	private lateinit var signalLocations: List<RegisteredControlSignal>

	private constructor(multiblockEntity: MultiblockEntity, signalLocations: List<Vec3i>) : this(multiblockEntity) {
		this.signalLocations = signalLocations.map { RegisteredControlSignal(it.x, it.y, it.z) }
	}

	private inner class RegisteredControlSignal(
		val offsetRight: Int,
		val offsetUp: Int,
		val offsetForward: Int
	) {
		val realPosition get() = multiblockEntity.getPosRelative(offsetRight, offsetUp, offsetForward)

		fun getDirectSignal(): Int? {
			val (x, y, z) = realPosition
			if (getBlockDataSafe(multiblockEntity.world, x, y, z)?.customBlock?.key != CustomBlockKeys.REDSTONE_CONTROL_PORT) return null
			return multiblockEntity.world.minecraft.getDirectSignalTo(BlockPos(x, y, z))
		}

		fun getStrongestIndirectSignal(): Int? {
			val (x, y, z) = realPosition
			if (getBlockDataSafe(multiblockEntity.world, x, y, z)?.customBlock?.key != CustomBlockKeys.REDSTONE_CONTROL_PORT) return null
			return multiblockEntity.world.minecraft.getBestNeighborSignal(BlockPos(x, y, z))
		}

		fun isDirectlyPowered(): Boolean? {
			return (getDirectSignal() ?: return null) > 0
		}

		fun isIndirectlyPowered(): Boolean? {
			val (x, y, z) = realPosition
			if (getBlockDataSafe(multiblockEntity.world, x, y, z)?.customBlock?.key != CustomBlockKeys.REDSTONE_CONTROL_PORT) return null
			return multiblockEntity.world.minecraft.hasNeighborSignal(BlockPos(x, y, z))
		}
	}

	fun getStrongestSignal(): Int? {
		return signalLocations.mapNotNull { it.getDirectSignal() }.maxOrNull()
	}

	fun getStrongestIndirectSignal(): Int? {
		return signalLocations.mapNotNull { it.getStrongestIndirectSignal() }.maxOrNull()
	}

	fun hasAnyDirectPower(): Boolean {
		return signalLocations.any { it.isDirectlyPowered() ?: false }
	}

	fun hasAnyIndirectPower(): Boolean {
		return signalLocations.any { it.isIndirectlyPowered() ?: false }
	}

	companion object {
		fun builder(entity: MultiblockEntity): Builder = Builder(entity)
	}

	class Builder(private val entity: MultiblockEntity) {
		private val signalLocations = mutableListOf<Vec3i>()

		fun addSignalInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			signalLocations.add(Vec3i(offsetRight, offsetUp, offsetForward))
			return this
		}

		fun addSignInputs(): Builder {
			addSignalInput(0, -1, -1)
			addSignalInput(0, 1, -1)
			addSignalInput(1, 0, -1)
			addSignalInput(-1, 0, -1)
			addSignalInput(0, 0, -2)
			return this
		}

		fun build(): ControlSignalManager = ControlSignalManager(entity, signalLocations)
	}
}
