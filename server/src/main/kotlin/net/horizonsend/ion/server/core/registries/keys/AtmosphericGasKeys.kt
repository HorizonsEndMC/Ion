package net.horizonsend.ion.server.core.registries.keys

import net.horizonsend.ion.server.core.registries.IonRegistries
import net.horizonsend.ion.server.features.gas.type.Gas
import net.horizonsend.ion.server.features.gas.type.GasFuel
import net.horizonsend.ion.server.features.gas.type.GasOxidizer

object AtmosphericGasKeys : KeyRegistry<Gas>(IonRegistries.ATMOSPHERIC_GAS, Gas::class) {
	val HYDROGEN = registerTypedKey<GasFuel>("HYDROGEN")
	val NITROGEN = registerTypedKey<GasFuel>("NITROGEN")
	val METHANE = registerTypedKey<GasFuel>("METHANE")
	val OXYGEN = registerTypedKey<GasOxidizer>("OXYGEN")
	val CHLORINE = registerTypedKey<GasOxidizer>("CHLORINE")
	val FLUORINE = registerTypedKey<GasOxidizer>("FLUORINE")
	val HELIUM = registerTypedKey<Gas>("HELIUM")
	val CARBON_DIOXIDE = registerTypedKey<Gas>("CARBON_DIOXIDE")
}
