package net.horizonsend.ion.server.features.transport.nodes.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.transport.util.CacheType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey

class InputsData private constructor (val holder: MultiblockEntity, private val inputs: List<BuiltInputData>){
	fun registerInputs() {
		for (input in inputs) {
			input.register(holder.manager.getInputManager(), holder)
		}
	}

	fun releaseInputs() {
		for (input in inputs) {
			input.release(holder.manager.getInputManager(), holder)
		}
	}

	companion object {
		fun builder(holder: MultiblockEntity): Builder = Builder(holder)
	}

	data class BuiltInputData(
		private val type: CacheType,
		private val offsetRight: Int,
		private val offsetUp: Int,
		private val offsetForward: Int,
	) {
		private fun getRealPos(holder: MultiblockEntity): BlockKey {
			val newPos = getRelative(
				origin = holder.localVec3i,
				forwardFace = holder.structureDirection,
				right = offsetRight,
				up = offsetUp,
				forward = offsetForward
			)
			return toBlockKey(newPos)
		}

		fun register(manager: InputManager, holder: MultiblockEntity) {
			manager.registerInput(type, getRealPos(holder), holder)
		}

		fun release(manager: InputManager, holder: MultiblockEntity) {
			manager.deRegisterInput(type, getRealPos(holder), holder)
		}
	}

	class Builder(val holder: MultiblockEntity) {
		private val data: MutableList<BuiltInputData> = mutableListOf()

		private fun addInput(type: CacheType, offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			data.add(BuiltInputData(type, offsetRight, offsetUp, offsetForward))

			return this
		}

		fun addPowerInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			return addInput(CacheType.POWER, offsetRight, offsetUp, offsetForward)
		}

		fun addFluidInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			return addInput(CacheType.FLUID, offsetRight, offsetUp, offsetForward)
		}

		fun build(): InputsData {
			return InputsData(holder, data)
		}
	}
}
