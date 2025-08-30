package net.horizonsend.ion.server.core.registration.keys

import net.horizonsend.ion.server.features.space.signatures.SignatureSpawner

object SignatureSpawnerKeys : KeyRegistry<SignatureSpawner>(RegistryKeys.SIGNATURE_SPAWNER, SignatureSpawner::class) {
    val COMET_SMALL_SPAWNER = registerKey("COMET_SMALL_SPAWNER")
    val COMET_MEDIUM_SPAWNER = registerKey("COMET_MEDIUM_SPAWNER")
}