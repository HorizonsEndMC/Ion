package net.horizonsend.ion.server.core.registries.keys

import net.horizonsend.ion.server.configuration.StarshipBalancing
import net.horizonsend.ion.server.core.registries.IonRegistries
import net.horizonsend.ion.server.features.starship.type.StarshipType

object StarshipTypeKeys : KeyRegistry<StarshipType<*>>(IonRegistries.STARSHIP_TYPE, StarshipType::class) {
	val STARFIGHTER = registerTypedKey<StarshipType<StarshipBalancing>>("STARFIGHTER")
	val INTERCEPTOR = registerTypedKey<StarshipType<StarshipBalancing>>("INTERCEPTOR")
	val GUNSHIP = registerTypedKey<StarshipType<StarshipBalancing>>("GUNSHIP")
	val CORVETTE = registerTypedKey<StarshipType<StarshipBalancing>>("CORVETTE")
	val LOGISTICS_CORVETTE = registerTypedKey<StarshipType<StarshipBalancing>>("LOGISTICS_CORVETTE")
	val FRIGATE = registerTypedKey<StarshipType<StarshipBalancing>>("FRIGATE")
	val DESTROYER = registerTypedKey<StarshipType<StarshipBalancing>>("DESTROYER")
	val CRUISER = registerTypedKey<StarshipType<StarshipBalancing>>("CRUISER")
	val BATTLECRUISER = registerTypedKey<StarshipType<StarshipBalancing>>("BATTLECRUISER")
	val BATTLESHIP = registerTypedKey<StarshipType<StarshipBalancing>>("BATTLESHIP")
	val DREADNOUGHT = registerTypedKey<StarshipType<StarshipBalancing>>("DREADNOUGHT")

	val TANK = registerTypedKey<StarshipType<StarshipBalancing>>("TANK")

	val SHUTTLE = registerTypedKey<StarshipType<StarshipBalancing>>("SHUTTLE")
	val TRANSPORT = registerTypedKey<StarshipType<StarshipBalancing>>("TRANSPORT")
	val LIGHT_FREIGHTER = registerTypedKey<StarshipType<StarshipBalancing>>("LIGHT_FREIGHTER")
	val MEDIUM_FREIGHTER = registerTypedKey<StarshipType<StarshipBalancing>>("MEDIUM_FREIGHTER")
	val HEAVY_FREIGHTER = registerTypedKey<StarshipType<StarshipBalancing>>("HEAVY_FREIGHTER")
	val BARGE = registerTypedKey<StarshipType<StarshipBalancing>>("BARGE")

	val SPEEDER = registerTypedKey<StarshipType<StarshipBalancing>>("SPEEDER")
	val PLATFORM = registerTypedKey<StarshipType<StarshipBalancing>>("PLATFORM")
	val UNIDENTIFIEDSHIP = registerTypedKey<StarshipType<StarshipBalancing>>("UNIDENTIFIEDSHIP")
}
