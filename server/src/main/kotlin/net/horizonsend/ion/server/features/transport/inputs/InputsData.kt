package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey

class InputsData private constructor (val holder: MultiblockEntity, val inputs: List<BuiltInputData>){
	fun registerInputs() {
		for (input in inputs) {
			input.register(holder.manager.getInputManager(), input.inputCreator.invoke(holder))
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
		private val type: InputType,
		val offsetRight: Int,
		val offsetUp: Int,
		val offsetForward: Int,
		val inputCreator: (MultiblockEntity) -> RegisteredInput
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

		fun register(manager: InputManager, input: RegisteredInput) {
			manager.registerInput(type, getRealPos(input.holder), input)
		}

		fun release(manager: InputManager, entity: MultiblockEntity) {
			manager.deRegisterInput(type, getRealPos(entity), entity)
		}

		fun get(entity: MultiblockEntity): RegisteredInput? {
			val realPos = getRealPos(entity)
			return entity.manager.getInputManager().getInputs(type, realPos).firstOrNull { input -> input.holder == entity }
		}
	}

	class Builder(val holder: MultiblockEntity) {
		private val data: MutableList<BuiltInputData> = mutableListOf()

		fun addInput(type: InputType, offsetRight: Int, offsetUp: Int, offsetForward: Int, inputCreator: (MultiblockEntity) -> RegisteredInput): Builder {
			data.add(BuiltInputData(type, offsetRight, offsetUp, offsetForward, inputCreator))

			return this
		}

		fun addPowerInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			return addInput(InputType.POWER, offsetRight, offsetUp, offsetForward) { RegisteredInput.Simple(it) }
		}

		fun addFluidInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			return addInput(InputType.FLUID, offsetRight, offsetUp, offsetForward) { RegisteredInput.Simple(it) }
		}

		fun build(): InputsData {
			return InputsData(holder, data)
		}
	}
}
