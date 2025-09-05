package net.horizonsend.ion.server.features.multiblock.entity.type.fluids.storage

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.transport.fluids.FluidStack
import net.horizonsend.ion.server.features.transport.fluids.FluidType
import net.horizonsend.ion.server.features.transport.fluids.properties.FluidCategory

sealed interface FluidRestriction {
	fun canAdd(fluid: FluidStack): Boolean
	fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean

	fun canRemove(fluid: FluidStack): Boolean

	data object Unlimited: FluidRestriction {
		override fun canAdd(fluid: FluidStack): Boolean {
			return true
		}
		override fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
			return true
		}

		override fun canRemove(fluid: FluidStack): Boolean {
			return true
		}
	}

	class FluidCategoryWhitelist(val allowedCategories: Set<FluidCategory>): FluidRestriction {
		override fun canAdd(fluid: FluidStack): Boolean {
			return allowedCategories.intersect(fluid.type.getValue().categories.toSet()).isNotEmpty()
		}
		override fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
			return allowedCategories.intersect(type.getValue().categories.toSet()).isNotEmpty()
		}

		override fun canRemove(fluid: FluidStack): Boolean {
			return true
		}
	}

	class FluidCategoryBlacklist(val disallowedCategories: Set<FluidCategory>): FluidRestriction {
		override fun canAdd(fluid: FluidStack): Boolean {
			return disallowedCategories.intersect(fluid.type.getValue().categories.toSet()).isEmpty()
		}
		override fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
			return disallowedCategories.intersect(type.getValue().categories.toSet()).isEmpty()
		}

		override fun canRemove(fluid: FluidStack): Boolean {
			return true
		}
	}

	class FluidTypeWhitelist(val allowedFluids: Set<IonRegistryKey<FluidType, out FluidType>>): FluidRestriction {
		override fun canAdd(fluid: FluidStack): Boolean {
			return allowedFluids.contains(fluid.type)
		}
		override fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
			return allowedFluids.contains(type)
		}

		override fun canRemove(fluid: FluidStack): Boolean {
			return true
		}
	}

	class FluidTypeBlacklist(val disallowedFluids: Set<IonRegistryKey<FluidType, out FluidType>>): FluidRestriction {
		override fun canAdd(fluid: FluidStack): Boolean {
			return !disallowedFluids.contains(fluid.type)
		}
		override fun canAdd(type: IonRegistryKey<FluidType, out FluidType>): Boolean {
			return !disallowedFluids.contains(type)
		}

		override fun canRemove(fluid: FluidStack): Boolean {
			return true
		}
	}
}
