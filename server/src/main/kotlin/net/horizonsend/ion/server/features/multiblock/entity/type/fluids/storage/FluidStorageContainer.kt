package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.NamespacedKey

/**
 * A wrapper around the internal storage that contains information for displaying and saving the resources.
 **/
class FluidStorageContainer private constructor(
	val name: String,
	val displayName: Component,
	val namespacedKey: NamespacedKey,
	val capacity: Double,
	val restriction: FluidRestriction,
) {
	private var contentsUnsafe = FluidStack.empty()
		@Synchronized
		get
		@Synchronized
		set

	constructor(
		data: PersistentMultiblockData,
		name: String,
		displayName: Component,
		namespacedKey: NamespacedKey,
		capacity: Double,
		restriction: FluidRestriction,
	) : this(name, displayName, namespacedKey, capacity, restriction) {
		load(data)
	}

	fun load(data: PersistentMultiblockData): FluidStorageContainer {
		contentsUnsafe = data.getAdditionalData(namespacedKey, FluidStack) ?: return this
		runUpdates()
		return this
	}

	fun save(destination: PersistentMultiblockData) {
		destination.addAdditionalData(namespacedKey, FluidStack, contentsUnsafe)
	}

	fun canAdd(fluidStack: FluidStack): Boolean {
		if (contentsUnsafe.isEmpty()) return true

		if (contentsUnsafe.type != fluidStack.type) return false

		return restriction.canAdd(fluidStack)
	}

	fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
		if (contentsUnsafe.isEmpty()) return true
		if (contentsUnsafe.type != type) return false

		return restriction.canAdd(type)
	}

	fun hasRoomFor(fluidStack: FluidStack): Boolean {
		return fluidStack.amount + contentsUnsafe.amount <= capacity
	}

	fun addFluid(stack: FluidStack, location: Location?): Double {
		val newQuantity = minOf(getRemainingRoom(), stack.amount)
		val toAdd = stack.asAmount(newQuantity)

		contentsUnsafe.combine(toAdd, location)

		runUpdates()

		return stack.amount - newQuantity
	}

	fun setContents(fluidStack: FluidStack) {
		contentsUnsafe = fluidStack
		runUpdates()
	}

	fun getContents(): FluidStack {
		return contentsUnsafe
	}

	fun getRemainingRoom(): Double {
		return capacity - contentsUnsafe.amount
	}

	/** Returns amount not removed */
	fun removeAmount(amount: Double): Double {
		val toRemove = minOf(amount, contentsUnsafe.amount)

		val notRemoved = amount - toRemove

		contentsUnsafe.amount -= toRemove
		runUpdates()

		return notRemoved
	}

	fun clear() {
		contentsUnsafe = FluidStack.empty()
		runUpdates()
	}

	override fun toString(): String {
		return "Container[name= $name, key= $namespacedKey, storage= $contentsUnsafe]"
	}

	private val updateListeners: MutableList<(FluidStorageContainer) -> Unit> = mutableListOf()

	fun registerUpdateListener(listener: (FluidStorageContainer) -> Unit) {
		updateListeners.add(listener)
	}

	fun runUpdates() {
		Tasks.async {
			updateListeners.forEach { t -> t.invoke(this) }
		}
	}

	companion object {
		fun createEmpty(
			name: String,
			displayName: Component,
			namespacedKey: NamespacedKey,
			capacity: Double,
			restriction: FluidRestriction
		) = FluidStorageContainer(name, displayName, namespacedKey, capacity, restriction)
	}
}
