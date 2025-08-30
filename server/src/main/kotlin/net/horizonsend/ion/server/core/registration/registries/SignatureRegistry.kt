package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureKeys
import net.horizonsend.ion.server.features.space.signatures.Signature
import net.kyori.adventure.text.Component

class SignatureRegistry : Registry<Signature>(RegistryKeys.SIGNATURES) {
    override fun getKeySet(): KeyRegistry<Signature> = SignatureKeys

    override fun boostrap() {
        register(SignatureKeys.COMET_SMALL, Signature(
            key = SignatureKeys.COMET_SMALL,
            displayName = Component.text("Small Comet"),
            detectionRange = 5000
        ))

        register(SignatureKeys.COMET_MEDIUM, Signature(
            key = SignatureKeys.COMET_MEDIUM,
            displayName = Component.text("Medium Comet"),
            detectionRange = 3000
        ))
    }
}