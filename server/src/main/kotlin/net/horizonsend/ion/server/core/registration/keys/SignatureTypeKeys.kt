package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.space.signatures.SignatureType

object SignatureTypeKeys : KeyRegistry<SignatureType>(RegistryKeys.SIGNATURE_TYPE, SignatureType::class) {
    val COMET_SMALL = registerKey("COMET_SMALL")
    val COMET_MEDIUM = registerKey("COMET_MEDIUM")
}