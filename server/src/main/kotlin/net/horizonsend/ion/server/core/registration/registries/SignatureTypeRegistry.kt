package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.space.signatures.PersistentSignatureType
import net.horizonsend.ion.server.features.space.signatures.SchematicSignatureType
import net.horizonsend.ion.server.features.space.signatures.SignatureType
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.kyori.adventure.text.Component
import java.time.Duration

class SignatureTypeRegistry : Registry<SignatureType>(RegistryKeys.SIGNATURE_TYPE) {
    override fun getKeySet(): KeyRegistry<SignatureType> = SignatureTypeKeys

    override fun boostrap() {
        register(SignatureTypeKeys.COMET_SMALL, PersistentSignatureType(
            key = SignatureTypeKeys.COMET_SMALL,
            displayName = Component.text("Small Comet"),
            maximumPerServer = 5,
            minSpawnTimeMinutes = Duration.ofMinutes(15L),
            maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			detectionRange = 500,
			interactRange = 200,
			despawnTimeMinutes = Duration.ofMinutes(30L),
        ))

        register(SignatureTypeKeys.COMET_MEDIUM, PersistentSignatureType(
            key = SignatureTypeKeys.COMET_MEDIUM,
            displayName = Component.text("Medium Comet"),
            maximumPerServer = 3,
            minSpawnTimeMinutes = Duration.ofMinutes(20L),
            maxSpawnTimeMinutes = Duration.ofMinutes(60L),
			detectionRange = 500,
			interactRange = 200,
			despawnTimeMinutes = Duration.ofMinutes(30L),
		))

		register(SignatureTypeKeys.ASTEROID_FIELD, SchematicSignatureType(
			key = SignatureTypeKeys.ASTEROID_FIELD,
			displayName = Component.text("Asteroid Field"),
			minSpawnTimeMinutes = Duration.ofHours(2L),
			maxSpawnTimeMinutes = Duration.ofHours(4L),
			detectionRange = 500,
			schematicNames = WeightedRandomList(
				"scordite_field" to 35,
				"vanadium_field" to 35,
				"zircon_field" to 25,
				"atavum_field" to 5,
			),
		))

		register(SignatureTypeKeys.WRECK_SITE, SchematicSignatureType(
			key = SignatureTypeKeys.WRECK_SITE,
			displayName = Component.text("Wreck Site"),
			minSpawnTimeMinutes = Duration.ofHours(1L),
			maxSpawnTimeMinutes = Duration.ofHours(3L),
			detectionRange = 500,
			schematicNames = WeightedRandomList(
				"wreck_site_1" to 33,
				"wreck_site_2" to 33,
				"wreck_site_3" to 34,
			),
		))
    }
}
