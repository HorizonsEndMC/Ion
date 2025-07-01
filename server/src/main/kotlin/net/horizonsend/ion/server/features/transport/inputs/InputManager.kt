package net.horizonsend.ion.server.features.transport.inputs

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class InputManager {
	private val typeManagers = ConcurrentHashMap<InputType<*>, TypeManager<*>>()

	private fun <T : RegisteredInput> getTypeManager(type: InputType<T>): TypeManager<T> {
		@Suppress("UNCHECKED_CAST")
		return typeManagers.getOrPut(type) { TypeManager(this, type) } as TypeManager<T>
	}

	fun <T : RegisteredInput> registerInput(type: InputType<T>, location: BlockKey, input: T) {
		getTypeManager(type).add(location, input)
	}

	fun deRegisterInput(type: InputType<*>, location: BlockKey, holder: MultiblockEntity) {
		getTypeManager(type).remove(location, holder)
	}

	fun <T : RegisteredInput> getInputs(type: InputType<T>, location: BlockKey): ObjectOpenHashSet<T> {
		return getTypeManager(type).getAllHolders(location)
	}

	fun <T : RegisteredInput> getInputData(type: InputType<T>, location: BlockKey): TypeManager.InputData<T>? =
		getTypeManager(type).getRaw(location)

	fun getLocations(type: InputType<*>) = getTypeManager(type).getAllLocations()

	class TypeManager<T : RegisteredInput>(val manager: InputManager, val type: InputType<T>) {
		private val inputLocations = ConcurrentHashMap<BlockKey, InputData<T>>()

		fun getAllLocations() = inputLocations.keys

		fun getRaw(location: BlockKey): InputData<T>? = inputLocations[location]

		fun add(location: BlockKey, data: T) {
			when (val present: InputData<T>? = inputLocations[location]) {
				is SingleMultiblockInput -> inputLocations[location] = SharedMultiblockInput.of(present.input, data)
				is SharedMultiblockInput -> present.add(data)
				null -> inputLocations[location] = SingleMultiblockInput(data)
			}
		}

		fun remove(location: BlockKey, input: RegisteredInput) {
			when (val present: InputData<T>? = inputLocations.get(location)) {
				is SingleMultiblockInput -> if (present.input == input) inputLocations.remove(location)
				is SharedMultiblockInput -> present.remove(input)
				null -> return
			}
		}

		fun remove(location: BlockKey, holder: MultiblockEntity) {
			when (val present: InputData<T>? = inputLocations.get(location)) {
				is SingleMultiblockInput -> if (present.input.holder == holder) inputLocations.remove(location)
				is SharedMultiblockInput -> present.remove(holder)
				null -> return
			}
		}

		fun getAllHolders(location: BlockKey): ObjectOpenHashSet<T> {
			return inputLocations.get(location)?.getInputs() ?: ObjectOpenHashSet()
		}

		fun removeAll(location: BlockKey) {
			inputLocations.remove(location)
		}

		fun contains(location: BlockKey): Boolean {
			return inputLocations.contains(location)
		}

		sealed interface InputData<T : RegisteredInput> {
			fun contains(holder: MultiblockEntity): Boolean
			fun getInputs(): ObjectOpenHashSet<T>
		}

		data class SingleMultiblockInput<T : RegisteredInput>(val input: T) : InputData<T> {
			override fun contains(holder: MultiblockEntity): Boolean {
				return this.input == holder
			}

			override fun getInputs(): ObjectOpenHashSet<T> {
				return ObjectOpenHashSet.of(input)
			}
		}

		class SharedMultiblockInput<T : RegisteredInput> : InputData<T> {
			private val holders: ObjectOpenHashSet<T> = ObjectOpenHashSet()

			override fun contains(holder: MultiblockEntity): Boolean {
				return holders.any { input -> input.holder == holder }
			}

			override fun getInputs(): ObjectOpenHashSet<T> {
				return ObjectOpenHashSet(holders)
			}

			fun add(input: T) {
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
				fun <T : RegisteredInput> of(vararg inputs: T): SharedMultiblockInput<T> {
					val new = SharedMultiblockInput<T>()
					inputs.forEach(new::add)
					return new
				}
			}
		}
	}
}
