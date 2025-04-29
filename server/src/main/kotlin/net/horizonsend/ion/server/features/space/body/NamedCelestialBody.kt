package net.horizonsend.ion.server.features.space.body

import net.horizonsend.ion.common.utils.NavigationObject
import java.util.Locale

interface NamedCelestialBody : NavigationObject {
	val id get() = name.lowercase(Locale.getDefault()).replace(" ", "")
}
