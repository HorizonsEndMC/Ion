package net.horizonsend.ion.server.features.custom.items.type.tool.mods

import net.horizonsend.ion.server.core.registries.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import kotlin.reflect.KClass

interface ApplicationPredicate {
	fun canApplyTo(customItem: CustomItem): Boolean

	class ClassPredicate(val customItemClass: KClass<out CustomItem>) : ApplicationPredicate {
		override fun canApplyTo(customItem: CustomItem): Boolean {
			return customItemClass.isInstance(customItem)
		}
	}

	class SpecificPredicate(val customItem: IonRegistryKey<CustomItem>) : ApplicationPredicate {
		override fun canApplyTo(customItem: CustomItem): Boolean {
			return customItem.key == this.customItem
		}
	}
}
