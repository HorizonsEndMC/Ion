package net.starlegacy.feature.space

interface NamedCelestialBody {
    val name: String

    val id get() = name.toLowerCase().replace(" ", "")
}
