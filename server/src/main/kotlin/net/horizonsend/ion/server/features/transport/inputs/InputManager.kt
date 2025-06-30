package net.horizonsend.ion.server.features.transport.inputs

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class InputManager {
	private val typeManagers = ConcurrentHashMap<InputType, TypeManager>()

	private fun getTypeManager(type: InputType): TypeManager {
		return typeManagers.getOrPut(type) { TypeManager(this, type) }
	}

	fun registerInput(type: InputType, location: BlockKey, input: RegisteredInput) {
		getTypeManager(type).add(location, input)
	}

	fun deRegisterInput(type: InputType, location: BlockKey, holder: MultiblockEntity) {
		getTypeManager(type).remove(location, holder)
	}

	fun getInputs(type: InputType, location: BlockKey): ObjectOpenHashSet<RegisteredInput> {
		return getTypeManager(type).getAllHolders(location)
	}

	fun getInputData(type: InputType, location: BlockKey): TypeManager.InputData? =
		getTypeManager(type).getRaw(location)

	fun getLocations(type: InputType) = getTypeManager(type).getAllLocations()

	class TypeManager(val manager: InputManager, val type: InputType) {
		private val inputLocations = ConcurrentHashMap<BlockKey, InputData>()

		fun getAllLocations() = inputLocations.keys

		fun getRaw(location: BlockKey): InputData? = inputLocations[location]

		fun add(location: BlockKey, data: RegisteredInput) {
			when (val present: InputData? = inputLocations[location]) {
				is SingleMultiblockInput -> inputLocations[location] = SharedMultiblockInput.of(present.input, data)
				is SharedMultiblockInput -> present.add(data)
				null -> inputLocations[location] = SingleMultiblockInput(data)
			}
		}

		fun remove(location: BlockKey, input: RegisteredInput) {
			when (val present: InputData? = inputLocations.get(location)) {
				is SingleMultiblockInput -> if (present.input == input) inputLocations.remove(location)
				is SharedMultiblockInput -> present.remove(input)
				null -> return
			}
		}

		fun remove(location: BlockKey, holder: MultiblockEntity) {
			when (val present: InputData? = inputLocations.get(location)) {
				is SingleMultiblockInput -> if (present.input.holder == holder) inputLocations.remove(location)
				is SharedMultiblockInput -> present.remove(holder)
				null -> return
			}
		}

		fun getAllHolders(location: BlockKey): ObjectOpenHashSet<RegisteredInput> {
			return inputLocations.get(location)?.getHolders() ?: ObjectOpenHashSet()
		}

		fun removeAll(location: BlockKey) {
			inputLocations.remove(location)
		}

		fun contains(location: BlockKey): Boolean {
			return inputLocations.contains(location)
		}

		sealed interface InputData {
			fun contains(holder: MultiblockEntity): Boolean
			fun getHolders(): ObjectOpenHashSet<RegisteredInput>
		}

		data class SingleMultiblockInput(val input: RegisteredInput) : InputData {
			override fun contains(holder: MultiblockEntity): Boolean {
				return this.input == holder
			}

			override fun getHolders(): ObjectOpenHashSet<RegisteredInput> {
				return ObjectOpenHashSet.of(input)
			}
		}

		class SharedMultiblockInput : InputData {
			private val holders: ObjectOpenHashSet<RegisteredInput> = ObjectOpenHashSet()

			override fun contains(holder: MultiblockEntity): Boolean {
				return holders.any { input -> input.holder == holder }
			}

			override fun getHolders(): ObjectOpenHashSet<RegisteredInput> {
				return ObjectOpenHashSet(holders)
			}

			fun add(input: RegisteredInput) {
				holders.add(input)
			}

			fun remove(input: RegisteredInput) {
				holders.remove(input)
			}

			fun remove(entity: MultiblockEntity) {
				holders.removeAll { it.holder == entity }
			}

			fun getAllHolders() = holders.clone()

			companion object {
				fun of(vararg inputs: RegisteredInput): SharedMultiblockInput {
					val new = SharedMultiblockInput()
					inputs.forEach(new::add)
					return new
				}
			}
		}
	}
}
