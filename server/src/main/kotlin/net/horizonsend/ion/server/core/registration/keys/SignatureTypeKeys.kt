package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.space.signatures.SignatureType

object SignatureTypeKeys : KeyRegistry<SignatureType>(RegistryKeys.SIGNATURE_TYPE, SignatureType::class) {
    val COMET_SMALL = registerKey("COMET_SMALL")
    val COMET_MEDIUM = registerKey("COMET_MEDIUM")
	val SCORDITE_FIELD = registerKey("SCORDITE_FIELD")
	val VANADIUM_FIELD = registerKey("VANADIUM_FIELD")
	val ZIRCON_FIELD = registerKey("ZIRCON_FIELD")
	val ATAVUM_FIELD = registerKey("ATAVUM_FIELD")
}
