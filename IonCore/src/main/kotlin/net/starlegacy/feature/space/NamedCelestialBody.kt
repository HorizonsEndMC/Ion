package net.starlegacy.feature.space

import java.util.Locale

interface NamedCelestialBody {
	val name: String

	val id get() = name.lowercase(Locale.getDefault()).replace(" ", "")
}
