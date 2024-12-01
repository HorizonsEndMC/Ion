package net.horizonsend.ion.server.features.transport.fluids

import org.bukkit.persistence.PersistentDataAdapterContext
import org.bukkit.persistence.PersistentDataType

object FluidPersistentDataType : PersistentDataType<String, Fluid> {
	override fun getComplexType(): Class<Fluid> = Fluid::class.java
	override fun getPrimitiveType(): Class<String> = String::class.java

	override fun fromPrimitive(primitive: String, context: PersistentDataAdapterContext): Fluid {
		return FluidRegistry.get(primitive)!!
	}

	override fun toPrimitive(complex: Fluid, context: PersistentDataAdapterContext): String {
		return complex.identifier
	}
}
