package net.horizonsend.ion.server.features.transport.nodes.inputs

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.transport.util.NetworkType
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class InputManager {
	private val typeManagers = ConcurrentHashMap<NetworkType, TypeManager>()

	private fun getTypeManager(type: NetworkType): TypeManager {
		return typeManagers.getOrPut(type) { TypeManager(this, type) }
	}

	fun registerInput(type: NetworkType, location: BlockKey, holder: MultiblockEntity) {
		getTypeManager(type).add(location, holder)
	}

	fun deRegisterInput(type: NetworkType, location: BlockKey, holder: MultiblockEntity) {
		getTypeManager(type).remove(location, holder)
	}

	fun getHolders(type: NetworkType, location: BlockKey): Set<MultiblockEntity> {
		return getTypeManager(type).getAllHolders(location)
	}

	fun getLocations(type: NetworkType) = getTypeManager(type).getAllLocations()

	class TypeManager(val manager: InputManager, val type: NetworkType) {
		private val inputLocations = Long2ObjectOpenHashMap<InputData>()

		fun getAllLocations() = inputLocations.keys

		fun add(location: BlockKey, holder: MultiblockEntity) {
			when (val present: InputData? = inputLocations.get(location)) {
				is SingleMultiblockInput -> inputLocations[location] = SharedMultiblockInput.of(present.holder, holder)
				is SharedMultiblockInput -> present.add(holder)
				null -> inputLocations[location] = SingleMultiblockInput(holder)
			}

			return
		}

		fun remove(location: BlockKey, holder: MultiblockEntity) {
			when (val present: InputData? = inputLocations.get(location)) {
				is SingleMultiblockInput -> if (present.holder == holder) inputLocations.remove(location)
				is SharedMultiblockInput -> present.remove(holder)
				null -> return
			}
		}

		fun getAllHolders(location: BlockKey): Set<MultiblockEntity> {
			return when (val present: InputData? = inputLocations.get(location)) {
				is SingleMultiblockInput -> setOf(present.holder)
				is SharedMultiblockInput -> present.getAllHolders()
				null -> setOf()
			}
		}

		fun removeAll(location: BlockKey) {
			inputLocations.remove(location)
		}

		fun contains(location: BlockKey): Boolean {
			return inputLocations.contains(location)
		}

		sealed interface InputData {
			fun contains(holder: MultiblockEntity): Boolean
		}

		data class SingleMultiblockInput(val holder: MultiblockEntity) : InputData {
			override fun contains(holder: MultiblockEntity): Boolean {
				return this.holder == holder
			}
		}

		class SharedMultiblockInput : InputData {
			private val holders: ObjectOpenHashSet<MultiblockEntity> = ObjectOpenHashSet()

			override fun contains(holder: MultiblockEntity): Boolean {
				return holders.contains(holder)
			}

			fun add(multiblockEntity: MultiblockEntity) {
				holders.add(multiblockEntity)
			}

			fun remove(multiblockEntity: MultiblockEntity) {
				holders.remove(multiblockEntity)
			}

			fun getAllHolders() = holders.clone()

			companion object {
				fun of(vararg entities: MultiblockEntity): SharedMultiblockInput {
					val new = SharedMultiblockInput()
					entities.forEach(new::add)
					return new
				}
			}
		}
	}
}
