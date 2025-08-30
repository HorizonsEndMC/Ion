package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.core.registration.IonRegistryKey

class SignatureSpawner(
    key: IonRegistryKey<SignatureSpawner, out SignatureSpawner>,
    private val signatureKey: IonRegistryKey<Signature, out Signature>,
    maxInServer: Int
) {
}