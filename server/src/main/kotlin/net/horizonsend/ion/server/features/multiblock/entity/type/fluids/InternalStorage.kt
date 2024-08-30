package net.horizonsend.ion.server.features.multiblock.entity.type.fluids

import net.horizonsend.ion.server.features.transport.fluids.PipedFluid
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.PDCSerializable
import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataContainer
import org.bukkit.persistence.PersistentDataType

/**
 * Fluid storing multiblocks can have a single, or multiple fluid storage capacities
 *
 * Each internal storage can hold a single type of fluid
 **/
abstract class InternalStorage : PDCSerializable<InternalStorage, InternalStorage.Companion> {
	abstract val containerType: ContainerType
	protected var amountUnsafe: Int = 0
	protected var fluidUnsafe: PipedFluid? = null

	abstract fun canStore(resource: PipedFluid, liters: Double): Boolean

	abstract fun getCapacity(): Int

	fun getStoredFluid(): PipedFluid? = fluidUnsafe

	fun addAmount(amount: Int) {
		setAmount(getAmount() + amount)
	}

	fun addAmount(fluid: PipedFluid, amount: Int) {
		if (getStoredFluid() != null && fluid != getStoredFluid()) return

		if (getStoredFluid() == null) setFluid(fluid)

		addAmount(amount)
	}

	fun getAmount(): Int = amountUnsafe

	fun setContents(fluid: PipedFluid, amount: Int) {
		setFluid(fluid)
		setAmount(amount)
	}

	fun setAmount(amount: Int) {
		val corrected = amount.coerceIn(0, getCapacity())

		this.amountUnsafe = corrected

		if (corrected == 0) setFluid(null)

		updateVisually()
	}

	fun setFluid(fluid: PipedFluid?) {
		this.fluidUnsafe = fluid

		updateVisually()
	}

	open fun updateVisually() {}

	fun getContents(): Pair<PipedFluid?, Int> = fluidUnsafe to getAmount()

	override val persistentDataType: Companion = Companion

	open fun saveAdditionalData(pdc: PersistentDataContainer) {}

	companion object : PersistentDataType<PersistentDataContainer, InternalStorage> {
		override fun getPrimitiveType(): Class<PersistentDataContainer> = PersistentDataContainer::class.java
		override fun getComplexType(): Class<InternalStorage> = InternalStorage::class.java

		override fun fromPrimitive(primitive: PersistentDataContainer, context: PersistentDataAdapterContext): InternalStorage {
			val type = primitive.get(NamespacedKeys.CONTAINER_TYPE, ContainerType.persistentDataType)!!
			return type.create(primitive)
		}

		override fun toPrimitive(complex: InternalStorage, context: PersistentDataAdapterContext): PersistentDataContainer {
			val pdc = context.newPersistentDataContainer()
			pdc.set(NamespacedKeys.CONTAINER_TYPE, ContainerType.persistentDataType, complex.containerType)
			pdc.set(NamespacedKeys.RESOURCE_CAPACITY_MAX, PersistentDataType.INTEGER, complex.getCapacity())

			val fluid = complex.getStoredFluid()
			if (fluid != null) {
				pdc.set(NamespacedKeys.FLUID, PersistentDataType.STRING, fluid.identifier)
				pdc.set(NamespacedKeys.RESOURCE_AMOUNT, PersistentDataType.INTEGER, complex.getAmount())
			}

			complex.saveAdditionalData(pdc)

			return pdc
		}
	}
}
