package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.kyori.adventure.text.Component

class Signature(
    key: IonRegistryKey<Signature, out Signature>,
    displayName: Component,
    detectionRange: Int,
) {
}