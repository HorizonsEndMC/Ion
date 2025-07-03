package net.horizonsend.ion.server.features.transport.inputs

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getRelative
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.toBlockKey

class IOData private constructor (val holder: MultiblockEntity, val inputs: List<BuiltInputData<*>>){
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

	fun <T : IOPort> getOfType(type: IOType<T>): List<BuiltInputData<T>> {
		return inputs.filter { data -> data.type == type }.filterIsInstance<BuiltInputData<T>>()
	}

	data class BuiltInputData<T : IOPort>(
		val type: IOType<T>,
		val offsetRight: Int,
		val offsetUp: Int,
		val offsetForward: Int,
		val inputCreator: (MultiblockEntity) -> T
	) {
		fun getRealPos(holder: MultiblockEntity): BlockKey {
			val newPos = getRelative(
				origin = holder.localVec3i,
				forwardFace = holder.structureDirection,
				right = offsetRight,
				up = offsetUp,
				forward = offsetForward
			)
			return toBlockKey(newPos)
		}

		fun register(manager: IOManager, holder: MultiblockEntity) {
			manager.registerPort(type, getRealPos(holder), inputCreator.invoke(holder))
		}

		fun release(manager: IOManager, entity: MultiblockEntity) {
			manager.deRegisterInput(type, getRealPos(entity), entity)
		}

		fun get(entity: MultiblockEntity): T? {
			val realPos = getRealPos(entity)
			// If the
			return entity.manager.getInputManager().getPorts(type, realPos).firstOrNull { input -> input.holder == entity && type.clazz.isInstance(input) } as? T
		}
	}

	class Builder(val holder: MultiblockEntity) {
		private val data: MutableList<BuiltInputData<*>> = mutableListOf()

		fun <T : IOPort> addPort(type: IOType<T>, offsetRight: Int, offsetUp: Int, offsetForward: Int, inputCreator: (MultiblockEntity) -> T): Builder {
			data.add(BuiltInputData(type, offsetRight, offsetUp, offsetForward, inputCreator))

			return this
		}

		fun addPowerInput(offsetRight: Int, offsetUp: Int, offsetForward: Int): Builder {
			return addPort(IOType.POWER, offsetRight, offsetUp, offsetForward) { IOPort.Simple(it) }
		}

		fun build(): IOData {
			return IOData(holder, data)
		}
	}
}
