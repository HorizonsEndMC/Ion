package net.horizonsend.ion.server.features.starship.type

import net.horizonsend.ion.server.core.registries.IonRegistryKey

sealed interface ComputerMenuConfiguration {
	//TODO
	class NormalIcon() : ComputerMenuConfiguration {

	}

	class IconWithSubclassses(vararg subclasses: IonRegistryKey<StarshipType<*>, out StarshipType<*>>) : ComputerMenuConfiguration {

	}
}
