package net.horizonsend.ion.server.features.transport.inputs

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import java.util.concurrent.ConcurrentHashMap

abstract class IOManager {
	private val typeManagers = ConcurrentHashMap<IOType<*>, TypeManager<*>>()

	private fun <T : IOPort> getTypeManager(type: IOType<T>): TypeManager<T> {
		@Suppress("UNCHECKED_CAST")
		return typeManagers.getOrPut(type) { TypeManager(this, type) } as TypeManager<T>
	}

	fun <T : IOPort> registerPort(type: IOType<T>, location: BlockKey, input: T) {
		getTypeManager(type).add(location, input)
	}

	fun deRegisterInput(type: IOType<*>, location: BlockKey, holder: MultiblockEntity) {
		getTypeManager(type).remove(location, holder)
	}

	fun <T : IOPort> getPorts(type: IOType<T>, location: BlockKey): ObjectOpenHashSet<T> {
		return getTypeManager(type).getAllHolders(location)
	}

	fun <T : IOPort> getIOData(type: IOType<T>, location: BlockKey): TypeManager.IOData<T>? =
		getTypeManager(type).getRaw(location)

	fun getLocations(type: IOType<*>) = getTypeManager(type).getAllLocations()

	class TypeManager<T : IOPort>(val manager: IOManager, val type: IOType<T>) {
		private val portLocations = ConcurrentHashMap<BlockKey, IOData<T>>()

		fun getAllLocations() = portLocations.keys

		fun getRaw(location: BlockKey): IOData<T>? = portLocations[location]

		fun add(location: BlockKey, data: T) {
			when (val present: IOData<T>? = portLocations[location]) {
				is SingleMultiblockIO -> portLocations[location] = SharedMultiblockIO.of(present.port, data)
				is SharedMultiblockIO -> present.add(data)
				null -> portLocations[location] = SingleMultiblockIO(data)
			}
		}

		fun remove(location: BlockKey, port: IOPort) {
			when (val present: IOData<T>? = portLocations.get(location)) {
				is SingleMultiblockIO -> if (present.port == port) portLocations.remove(location)
				is SharedMultiblockIO -> present.remove(port)
				null -> return
			}
		}

		fun remove(location: BlockKey, holder: MultiblockEntity) {
			when (val present: IOData<T>? = portLocations[location]) {
				is SingleMultiblockIO -> if (present.port.holder == holder) portLocations.remove(location)
				is SharedMultiblockIO -> present.remove(holder)
				null -> return
			}
		}

		fun getAllHolders(location: BlockKey): ObjectOpenHashSet<T> {
			return portLocations
				.get(location)
				?.getPorts()
				?: ObjectOpenHashSet()
		}

		fun removeAll(location: BlockKey) {
			portLocations.remove(location)
		}

		fun contains(location: BlockKey): Boolean {
			return portLocations.contains(location)
		}

		sealed interface IOData<T : IOPort> {
			fun contains(holder: MultiblockEntity): Boolean
			fun getPorts(): ObjectOpenHashSet<T>
		}

		data class SingleMultiblockIO<T : IOPort>(val port: T) : IOData<T> {
			override fun contains(holder: MultiblockEntity): Boolean {
				return this.port == holder
			}

			override fun getPorts(): ObjectOpenHashSet<T> {
				return ObjectOpenHashSet.of(port)
			}
		}

		class SharedMultiblockIO<T : IOPort> : IOData<T> {
			private val holders: ObjectOpenHashSet<T> = ObjectOpenHashSet()

			override fun contains(holder: MultiblockEntity): Boolean {
				return holders.any { port -> port.holder == holder }
			}

			override fun getPorts(): ObjectOpenHashSet<T> {
				return ObjectOpenHashSet(holders)
			}

			fun add(port: T) {
				holders.add(port)
			}

			fun remove(port: IOPort) {
				holders.remove(port)
			}

			fun remove(entity: MultiblockEntity) {
				holders.removeAll { it.holder == entity }
			}

			fun getAllHolders() = holders.clone()

			companion object {
				fun <T : IOPort> of(vararg port: T): SharedMultiblockIO<T> {
					val new = SharedMultiblockIO<T>()
					port.forEach(new::add)
					return new
				}
			}
		}
	}
}
