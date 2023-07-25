package net.horizonsend.ion.server.features.space

import java.util.Locale

interface NamedCelestialBody {
	val name: String

	val id get() = name.lowercase(Locale.getDefault()).replace(" ", "")
}
