package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.space.signatures.SignatureType
import net.kyori.adventure.text.Component
import java.time.Duration
import java.util.concurrent.TimeUnit

class SignatureTypeRegistry : Registry<SignatureType>(RegistryKeys.SIGNATURE_TYPE) {
    override fun getKeySet(): KeyRegistry<SignatureType> = SignatureTypeKeys

    override fun boostrap() {
        register(SignatureTypeKeys.COMET_SMALL, SignatureType(
            key = SignatureTypeKeys.COMET_SMALL,
            displayName = Component.text("Small Comet"),
            detectionRange = 5000,
            interactRange = 200,
            maximumPerServer = 5,
            minSpawnTimeMinutes = Duration.ofMinutes(15L),
            maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			despawnTimeMinutes = Duration.ofMinutes(30L),
			schematicName = "comet_small"
        ))

        register(SignatureTypeKeys.COMET_MEDIUM, SignatureType(
            key = SignatureTypeKeys.COMET_MEDIUM,
            displayName = Component.text("Medium Comet"),
            detectionRange = 3000,
            interactRange = 200,
            maximumPerServer = 3,
            minSpawnTimeMinutes = Duration.ofMinutes(20L),
            maxSpawnTimeMinutes = Duration.ofMinutes(60L),
			despawnTimeMinutes = Duration.ofMinutes(30L),
			schematicName = "comet_medium"
		))

		register(SignatureTypeKeys.SCORDITE_FIELD, SignatureType(
			key = SignatureTypeKeys.SCORDITE_FIELD,
			displayName = Component.text("Scordite Asteroid Field"),
			detectionRange = 5000,
			interactRange = 200,
			maximumPerServer = 5,
			minSpawnTimeMinutes = Duration.ofMinutes(15L),
			maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			despawnTimeMinutes = Duration.ofMinutes(0L),
			schematicName = "scordite_field"
		))

		register(SignatureTypeKeys.VANADIUM_FIELD, SignatureType(
			key = SignatureTypeKeys.VANADIUM_FIELD,
			displayName = Component.text("Vanadium Asteroid Field"),
			detectionRange = 5000,
			interactRange = 200,
			maximumPerServer = 5,
			minSpawnTimeMinutes = Duration.ofMinutes(15L),
			maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			despawnTimeMinutes = Duration.ofMinutes(0L),
			schematicName = "vanadium_field"
		))

		register(SignatureTypeKeys.ZIRCON_FIELD, SignatureType(
			key = SignatureTypeKeys.ZIRCON_FIELD,
			displayName = Component.text("Zircon Asteroid Field"),
			detectionRange = 5000,
			interactRange = 200,
			maximumPerServer = 5,
			minSpawnTimeMinutes = Duration.ofMinutes(15L),
			maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			despawnTimeMinutes = Duration.ofMinutes(0L),
			schematicName = "zircon_field"
		))

		register(SignatureTypeKeys.ATAVUM_FIELD, SignatureType(
			key = SignatureTypeKeys.ATAVUM_FIELD,
			displayName = Component.text("Atavum Asteroid Field"),
			detectionRange = 5000,
			interactRange = 200,
			maximumPerServer = 5,
			minSpawnTimeMinutes = Duration.ofMinutes(15L),
			maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			despawnTimeMinutes = Duration.ofMinutes(0L),
			schematicName = "atavum_field"
		))
    }
}
