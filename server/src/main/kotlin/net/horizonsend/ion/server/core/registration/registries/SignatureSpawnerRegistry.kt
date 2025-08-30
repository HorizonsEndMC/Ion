package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureSpawnerKeys
import net.horizonsend.ion.server.features.space.signatures.SignatureSpawner

class SignatureSpawnerRegistry : Registry<SignatureSpawner>(RegistryKeys.SIGNATURE_SPAWNER) {
    override fun getKeySet(): KeyRegistry<SignatureSpawner> = SignatureSpawnerKeys

    override fun boostrap() {
        register(SignatureSpawnerKeys.COMET_SMALL_SPAWNER, SignatureSpawner(
            key = SignatureSpawnerKeys.COMET_SMALL_SPAWNER,
            signatureKey = SignatureKeys.COMET_SMALL,
            maxInServer = 5
        ))

        register(SignatureSpawnerKeys.COMET_MEDIUM_SPAWNER, SignatureSpawner(
            key = SignatureSpawnerKeys.COMET_MEDIUM_SPAWNER,
            signatureKey = SignatureKeys.COMET_MEDIUM,
            maxInServer = 2
        ))
    }
}