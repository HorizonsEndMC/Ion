package net.horizonsend.ion.server.core.registration.registries

import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.keys.KeyRegistry
import net.horizonsend.ion.server.core.registration.keys.RegistryKeys
import net.horizonsend.ion.server.core.registration.keys.SignatureTypeKeys
import net.horizonsend.ion.server.features.space.signatures.PersistentBehavior
import net.horizonsend.ion.server.features.space.signatures.ScannableBehavior
import net.horizonsend.ion.server.features.space.signatures.SchematicBehavior
import net.horizonsend.ion.server.features.space.signatures.SignatureManager
import net.horizonsend.ion.server.features.space.signatures.SignatureType
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.blockKeyZ
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.block.Chest
import org.bukkit.persistence.PersistentDataType
import java.time.Duration

class SignatureTypeRegistry : Registry<SignatureType>(RegistryKeys.SIGNATURE_TYPE) {
	override fun getKeySet(): KeyRegistry<SignatureType> = SignatureTypeKeys

	override fun boostrap() {
		/*
		register(SignatureTypeKeys.COMET_SMALL, SignatureType(
			key = SignatureTypeKeys.COMET_SMALL,
			displayName = Component.text("Small Comet"),
			minSpawnTimeMinutes = Duration.ofMinutes(15L),
			maxSpawnTimeMinutes = Duration.ofMinutes(30L),
			persistent = PersistentBehaviour(
				maximumPerServer = 5,
				despawnTimeMinutes = Duration.ofMinutes(30L),
			),
		))

		register(SignatureTypeKeys.COMET_MEDIUM, SignatureType(
			key = SignatureTypeKeys.COMET_MEDIUM,
			displayName = Component.text("Medium Comet"),
			minSpawnTimeMinutes = Duration.ofMinutes(20L),
			maxSpawnTimeMinutes = Duration.ofMinutes(60L),
			persistent = PersistentBehaviour(
				maximumPerServer = 3,
				despawnTimeMinutes = Duration.ofMinutes(30L),
			),
		))
		*/

		register(SignatureTypeKeys.ASTEROID_FIELD, SignatureType(
			key = SignatureTypeKeys.ASTEROID_FIELD,
			displayName = Component.text("Asteroid Field"),
			minSpawnTime = Duration.ofHours(1L),
			maxSpawnTime = Duration.ofHours(3L),
			persistentBehavior = PersistentBehavior(
				maximumPerServer = 7,
				despawnTime = Duration.ofMinutes(480L),
			),
			schematicBehavior = SchematicBehavior(
				schematicNames = WeightedRandomList(
					"scordite_field_1" to 25,
					"scordite_field_2" to 25,
					"vanadium_field" to 35,
					"zircon_field" to 13,
					"atavum_field" to 2,
				),
			),
			scannableBehavior = ScannableBehavior(
				onScan = { signature, starship ->
					val pasteResult = signature.signatureType.schematicBehavior?.generateSchematic(signature.location, SignatureManager.schematicCache)
					if (pasteResult == false) {
						starship.serverError("Could not generate asteroid field; spawning a new asteroid field soon")
						IonRegistries.SIGNATURE_TYPE[signature.signatureType.key].nextSpawnTimeMillis = System.currentTimeMillis()
						signature.destroyNextTick = true
						return@ScannableBehavior
					}

					starship.success("Discovered an asteroid field at [${signature.location.blockX}, ${signature.location.blockY}, ${signature.location.blockZ}] in ${signature.location.world.name}")
					IonServer.logger.info("Generated asteroid field for ${starship.playerPilot?.name} at ${signature.location.blockX}, ${signature.location.blockY}, ${signature.location.blockZ} in ${signature.location.world.name}")
					signature.destroyNextTick = true
				}
			)
		))

		register(SignatureTypeKeys.WRECK_SITE, SignatureType(
			key = SignatureTypeKeys.WRECK_SITE,
			displayName = Component.text("Wreck Site"),
			minSpawnTime = Duration.ofMinutes(30L),
			maxSpawnTime = Duration.ofMinutes(90L),
			persistentBehavior = PersistentBehavior(
				maximumPerServer = 7,
				despawnTime = Duration.ofMinutes(480L),
			),
			schematicBehavior = SchematicBehavior(
				schematicNames = WeightedRandomList(
					"CorvetteHighN" to 2,
					"CorvetteHighS" to 2,
					"CorvetteHighE" to 2,
					"CorvetteHighW" to 2,
					"CorvetteLow01N" to 2,
					"CorvetteLow01S" to 2,
					"CorvetteLow01E" to 2,
					"CorvetteLow01W" to 2,
					"CorvetteLow02N" to 2,
					"CorvetteLow02S" to 2,
					"CorvetteLow02E" to 2,
					"CorvetteLow02W" to 2,
					"CorvetteMid01N" to 2,
					"CorvetteMid01S" to 2,
					"CorvetteMid01E" to 2,
					"CorvetteMid01W" to 2,
					"DestroyerHighN" to 1,
					"DestroyerHighS" to 1,
					"DestroyerHighE" to 1,
					"DestroyerHighW" to 1,
					"DestroyerLow01N" to 1,
					"DestroyerLow01S" to 1,
					"DestroyerLow01E" to 1,
					"DestroyerLow01W" to 1,
					"DestroyerLow02N" to 1,
					"DestroyerLow02S" to 1,
					"DestroyerLow02E" to 1,
					"DestroyerLow02W" to 1,
					"DestroyerMid01N" to 1,
					"DestroyerMid01S" to 1,
					"DestroyerMid01E" to 1,
					"DestroyerMid01W" to 1,
					"DestroyerMid02N" to 1,
					"DestroyerMid02S" to 1,
					"DestroyerMid02E" to 1,
					"DestroyerMid02W" to 1,
					"FrigateHighN" to 1,
					"FrigateHighS" to 1,
					"FrigateHighE" to 1,
					"FrigateHighW" to 1,
					"FrigateLow01N" to 1,
					"FrigateLow01S" to 1,
					"FrigateLow01E" to 1,
					"FrigateLow01W" to 1,
					"FrigateLow02N" to 1,
					"FrigateLow02S" to 1,
					"FrigateLow02E" to 1,
					"FrigateLow02W" to 1,
					"FrigateMid01N" to 1,
					"FrigateMid01S" to 1,
					"FrigateMid01E" to 1,
					"FrigateMid01W" to 1,
					"FrigateMid02N" to 1,
					"FrigateMid02S" to 1,
					"FrigateMid02E" to 1,
					"FrigateMid02W" to 1,
				),
				callback = { placedBlocks, world ->
					val chestKeys = placedBlocks.filter { blockKey ->
						val x = blockKeyX(blockKey)
						val y = blockKeyY(blockKey)
						val z = blockKeyZ(blockKey)
						world.getBlockAt(x, y, z).type == Material.CHEST
					}

					for (blockKey in chestKeys) {
						val x = blockKeyX(blockKey)
						val y = blockKeyY(blockKey)
						val z = blockKeyZ(blockKey)
						val block = world.getBlockAt(x, y, z)
						val chest = block.state as? Chest ?: continue
						chest.persistentDataContainer.set(NamespacedKeys.WRECK_CHEST, PersistentDataType.BOOLEAN, true)
						chest.update()
					}
				}
			),
			scannableBehavior = ScannableBehavior(
				onScan = { signature, starship ->
					val pasteResult = signature.signatureType.schematicBehavior?.generateSchematic(signature.location, SignatureManager.schematicCache)
					if (pasteResult == false) {
						starship.serverError("Could not generate wreck site; spawning a new wreck site soon")
						IonRegistries.SIGNATURE_TYPE[signature.signatureType.key].nextSpawnTimeMillis = System.currentTimeMillis()
						signature.destroyNextTick = true
						return@ScannableBehavior
					}

					starship.success("Discovered a wreck site at [${signature.location.blockX}, ${signature.location.blockY}, ${signature.location.blockZ}] in ${signature.location.world.name}")
					IonServer.logger.info("Generated wreck site for ${starship.playerPilot?.name} at ${signature.location.blockX}, ${signature.location.blockY}, ${signature.location.blockZ} in ${signature.location.world.name}")
					signature.destroyNextTick = true
				}
			)
		))
	}
}
