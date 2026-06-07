package net.horizonsend.ion.server.features.space.signatures

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.utils.text.colors.HEColorScheme.Companion.HE_LIGHT_BLUE
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.isInRange
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.kyori.adventure.text.Component.text
import org.bukkit.Bukkit
import org.bukkit.Location
import java.io.File
import java.util.Optional
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

object SignatureManager : IonServerComponent(true) {
    private const val MAX_SPAWN_ATTEMPTS = 30
    private const val MIN_DISTANCE_FROM_STATIONS = 500

    // Map of signatures, and their epoch spawn time in millis
    val activeSignatures = ConcurrentHashMap<Signature, Long>()

	val schematicCache: LoadingCache<File, Optional<Clipboard>> = CacheBuilder.newBuilder().build(
		CacheLoader.from { schematicFile ->
			val clipboard = readSchematic(schematicFile) ?: return@from Optional.empty<Clipboard>()
			return@from Optional.of(clipboard)
		}
	)

    override fun onEnable() {
        if (ConfigurationFiles.featureFlags().explorationSpawns) {
            for (signature in IonRegistries.SIGNATURE_TYPE.getAll()) {
                signature.calculateNewSpawnTime()
            }

            Tasks.syncRepeat(10 * 20L, 10 * 20L) {
				tickSpawners()
				tickCurrentSignatures()
			}
        }
    }

	private fun tickSpawners() {
		for (signatureType in IonRegistries.SIGNATURE_TYPE.getAll()) {
			val persistent = signatureType.persistentBehavior
			if (persistent != null && activeSignatures.count { it.key.signatureType == signatureType } >= persistent.maximumPerServer) continue

			if (signatureType.isReadyToSpawn()) {
				val signature = generateNewSignature(signatureType) ?: continue
				log.info("Signature ${signature.signatureType.displayName.plainText()} spawned in ${signature.location.world.name}, ${signature.location.x}, ${signature.location.y}, ${signature.location.z}")

				IonServer.server.sendMessage(
					text("A ${signature.signatureType.displayName.plainText()} has spawned somewhere in ${signature.location.world.name}!", HE_LIGHT_BLUE)
				)

				if (persistent != null) activeSignatures[signature] = System.currentTimeMillis()
			}
		}
	}

	private fun tickCurrentSignatures() {
		val currentTimeMillis = System.currentTimeMillis()

		activeSignatures.entries.removeIf { (signature, spawnTime) ->
			val despawnTime = signature.signatureType.persistentBehavior?.despawnTime ?: return@removeIf true
			currentTimeMillis > despawnTime.plusMillis(spawnTime).toMillis() || signature.destroyNextTick
		}
	}

	/**
	 * Returns true if [location] is within the radius of any active signature whose type matches [typeKey]
	 * and which has a RadiusBehavior. Same-world only.
	 */
	fun isWithinSignatureRadius(
		location: Location,
		typeKey: IonRegistryKey<SignatureType, out SignatureType>,
	): Boolean {
		return activeSignatures.keys.any { signature ->
			if (signature.signatureType.key != typeKey) return@any false
			if (signature.location.world != location.world) return@any false

			val radiusBehavior = signature.signatureType.radiusBehavior ?: return@any false
			location.isInRange(signature.location, radiusBehavior.radius)
		}
	}

	private fun generateNewSignature(signatureType: SignatureType): Signature? {
		val location = getValidSignatureLocation()

		if (location == null) {
			log.warn("Failed to find a valid spawn location for signature type $signatureType after $MAX_SPAWN_ATTEMPTS attempts")
			return null
		}

		return Signature(signatureType, location)
	}

    private fun getValidSignatureLocation(): Location? {
        var attempts = 0

        while (attempts < MAX_SPAWN_ATTEMPTS) {
            attempts += 1

            val spaceWorlds = Bukkit.getWorlds().filter { it.ion.hasFlag(WorldFlag.SPACE_WORLD) && it.ion.hasFlag(WorldFlag.ALLOW_SIGNATURE_SPAWNS) }
            val randomSpaceWorld = spaceWorlds.random()

            val worldBorderMinX = randomSpaceWorld.worldBorder.center.x - (randomSpaceWorld.worldBorder.size / 2)
            val worldBorderMaxX = randomSpaceWorld.worldBorder.center.x + (randomSpaceWorld.worldBorder.size / 2)
            val worldBorderMinZ = randomSpaceWorld.worldBorder.center.z - (randomSpaceWorld.worldBorder.size / 2)
            val worldBorderMaxZ = randomSpaceWorld.worldBorder.center.z + (randomSpaceWorld.worldBorder.size / 2)

            val potentialX = ThreadLocalRandom.current().nextDouble(worldBorderMinX, worldBorderMaxX)
            val potentialY = (randomSpaceWorld.maxHeight - randomSpaceWorld.minHeight).toDouble() / 2
            val potentialZ = ThreadLocalRandom.current().nextDouble(worldBorderMinZ, worldBorderMaxZ)

            // Do not spawn signature within gravity wells
            val massShadows = MassShadows.find(randomSpaceWorld, potentialX, potentialZ)
            if (!massShadows.isNullOrEmpty()) continue

            val stationsInWorld = SpaceStationCache.all().filter { station -> station.world == randomSpaceWorld.name }
            for (station in stationsInWorld) {
                if (distanceSquared(
                        station.x.toDouble(),
                        (randomSpaceWorld.maxHeight - randomSpaceWorld.minHeight).toDouble() / 2,
                        station.z.toDouble(),
                        potentialX,
                        potentialY,
                        potentialZ) <= (station.radius + MIN_DISTANCE_FROM_STATIONS).toDouble().pow(2)
                    ) continue
            }

            val capturableStationsInWorld = CapturableStationCache.stations.filter { station -> station.loc.world.name == randomSpaceWorld.name }
            for (capturableStation in capturableStationsInWorld) {
                if (distanceSquared(
                        capturableStation.loc.x,
                        (randomSpaceWorld.maxHeight - randomSpaceWorld.minHeight).toDouble() / 2,
                        capturableStation.loc.z,
                        potentialX,
                        potentialY,
                        potentialZ) <= (NATIONS_BALANCE.capturableStation.radius + MIN_DISTANCE_FROM_STATIONS).toDouble().pow(2)
                ) continue
            }

            return Location(randomSpaceWorld, potentialX, potentialY, potentialZ)
        }

        return null
    }
}
