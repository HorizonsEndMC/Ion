package net.horizonsend.ion.server.features.custom.items.components

class CustomItemComponentManager {
	private val components = mutableMapOf<CustomComponentType<out CustomItemComponent, out ComponentTypeData<*>>, ComponentTypeData<*>>()

	fun <T : CustomItemComponent, Z : ComponentTypeData<T>> addComponent(type: CustomComponentType<T, Z>, data: T) {
		type.storageType.storeData(components, type, data)
	}

	fun <T : CustomItemComponent> getComponent(type: CustomComponentType<T, ComponentTypeData.OnlyOne<T>>): T {
		val stored = components[type] ?: throw NullPointerException("Trying to access unregistered custom component")
		return type.castData(stored).entry
	}

	fun <T : CustomItemComponent> getComponents(type: CustomComponentType<T, ComponentTypeData.AllowMultiple<T>>): List<T> {
		val stored = components[type] ?: throw NullPointerException("Trying to access unregistered custom component")
		return type.castData(stored).entries
	}

	fun getAll(): Collection<CustomItemComponent> = components.flatMap { it.value.getValues() }

	sealed interface ComponentTypeData<T : CustomItemComponent> {
		fun getValues(): Collection<T>

		class OnlyOne<T : CustomItemComponent>(val entry: T) : ComponentTypeData<T> {
			override fun getValues(): Collection<T> = listOf(entry)
		}

		class AllowMultiple<T : CustomItemComponent>(val entries: MutableList<T> = mutableListOf()) : ComponentTypeData<T> {
			override fun getValues(): Collection<T> = entries
		}
	}

	enum class ComponentType {
		ONLY_ONE {
			override fun <T : CustomItemComponent, Z : ComponentTypeData<T>> storeData(
				store: MutableMap<CustomComponentType<out CustomItemComponent, out ComponentTypeData<*>>, ComponentTypeData<*>>,
				type: CustomComponentType<T, Z>,
				data: T
			) {
				store[type] = ComponentTypeData.OnlyOne(data)
			}
		},
		ALLOW_MULTIPLE {
			override fun <T : CustomItemComponent, Z : ComponentTypeData<T>> storeData(
				store: MutableMap<CustomComponentType<out CustomItemComponent, out ComponentTypeData<*>>, ComponentTypeData<out CustomItemComponent>>,
				type: CustomComponentType<T, Z>,
				data: T
			) {
				@Suppress("UNCHECKED_CAST")
				(store.getOrPut(type) { ComponentTypeData.AllowMultiple<T>() } as ComponentTypeData.AllowMultiple<T>).entries.add(data)
			}
		};

		abstract fun <T : CustomItemComponent, Z : ComponentTypeData<T>> storeData(
			store: MutableMap<CustomComponentType<out CustomItemComponent, out ComponentTypeData<*>>, ComponentTypeData<*>>,
			type: CustomComponentType<T, Z>,
			data: T
		)
	}
}
