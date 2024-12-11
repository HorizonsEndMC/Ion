package net.horizonsend.ion.server.features.custom.items.util

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentType
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponent
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager.ComponentTypeData

fun <T : CustomItemComponent, Z : ComponentTypeData<T>, C : NewCustomItem> C.withComponent(type: CustomComponentType<T, Z>, data: T): C {
	customComponents.addComponent(type, data)
	return this
}
