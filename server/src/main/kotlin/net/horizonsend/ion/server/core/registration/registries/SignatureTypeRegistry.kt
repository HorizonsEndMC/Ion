package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.space.signatures.SignatureType
import net.kyori.adventure.text.Component
import java.time.Duration

class SignatureTypeRegistry : Registry<SignatureType>(RegistryKeys.SIGNATURE_TYPE) {
    override fun getKeySet(): KeyRegistry<SignatureType> = SignatureTypeKeys

    override fun boostrap() {
        register(SignatureTypeKeys.COMET_SMALL, SignatureType(
            key = SignatureTypeKeys.COMET_SMALL,
            displayName = Component.text("Small Comet"),
            detectionRange = 5000,
            maximumPerServer = 5,
            minSpawnTimeMinutes = Duration.ofMinutes(15L),
            maxSpawnTimeMinutes = Duration.ofMinutes(30L)
        ))

        register(SignatureTypeKeys.COMET_MEDIUM, SignatureType(
            key = SignatureTypeKeys.COMET_MEDIUM,
            displayName = Component.text("Medium Comet"),
            detectionRange = 3000,
            maximumPerServer = 3,
            minSpawnTimeMinutes = Duration.ofMinutes(20L),
            maxSpawnTimeMinutes = Duration.ofMinutes(60L)
        ))
    }
}