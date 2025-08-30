package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.space.signatures.Signature

object SignatureKeys : KeyRegistry<Signature>(RegistryKeys.SIGNATURES, Signature::class) {
    val COMET_SMALL = registerKey("COMET_SMALL")
    val COMET_MEDIUM = registerKey("COMET_MEDIUM")
}