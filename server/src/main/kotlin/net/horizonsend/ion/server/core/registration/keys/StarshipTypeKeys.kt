package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.configuration.starship.StarshipTypeBalancing
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.starship.type.StarshipType

object StarshipTypeKeys : KeyRegistry<StarshipType<*>>(IonRegistries.STARSHIP_TYPE, StarshipType::class) {
	val STARFIGHTER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("STARFIGHTER")
	val INTERCEPTOR = registerTypedKey<StarshipType<StarshipTypeBalancing>>("INTERCEPTOR")
	val GUNSHIP = registerTypedKey<StarshipType<StarshipTypeBalancing>>("GUNSHIP")
	val CORVETTE = registerTypedKey<StarshipType<StarshipTypeBalancing>>("CORVETTE")
	val LOGISTICS_CORVETTE = registerTypedKey<StarshipType<StarshipTypeBalancing>>("LOGISTICS_CORVETTE")
	val FRIGATE = registerTypedKey<StarshipType<StarshipTypeBalancing>>("FRIGATE")
	val DESTROYER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("DESTROYER")
	val CRUISER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("CRUISER")
	val BATTLECRUISER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("BATTLECRUISER")
	val BATTLESHIP = registerTypedKey<StarshipType<StarshipTypeBalancing>>("BATTLESHIP")
	val DREADNOUGHT = registerTypedKey<StarshipType<StarshipTypeBalancing>>("DREADNOUGHT")

	val TANK = registerTypedKey<StarshipType<StarshipTypeBalancing>>("TANK")

	val SHUTTLE = registerTypedKey<StarshipType<StarshipTypeBalancing>>("SHUTTLE")
	val TRANSPORT = registerTypedKey<StarshipType<StarshipTypeBalancing>>("TRANSPORT")
	val LIGHT_FREIGHTER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("LIGHT_FREIGHTER")
	val MEDIUM_FREIGHTER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("MEDIUM_FREIGHTER")
	val HEAVY_FREIGHTER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("HEAVY_FREIGHTER")
	val BARGE = registerTypedKey<StarshipType<StarshipTypeBalancing>>("BARGE")

	val SPEEDER = registerTypedKey<StarshipType<StarshipTypeBalancing>>("SPEEDER")
	val PLATFORM = registerTypedKey<StarshipType<StarshipTypeBalancing>>("PLATFORM")
	val UNIDENTIFIEDSHIP = registerTypedKey<StarshipType<StarshipTypeBalancing>>("UNIDENTIFIEDSHIP")
}
