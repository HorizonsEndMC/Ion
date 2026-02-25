package net.horizonsend.ion.server.features.space.signatures

import com.google.common.cache.CacheBuilder
import com.google.common.cache.CacheLoader
import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.misc.KothStationCache
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.nations.region.Regions
import net.horizonsend.ion.server.features.nations.region.types.RegionFrontierTerritory
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.horizonsend.ion.server.miscellaneous.utils.readSchematic
import net.kyori.adventure.text.Component
import org.bukkit.Bukkit
import org.bukkit.Location
import org.slf4j.Logger
import java.io.File
import java.util.Optional
import java.util.concurrent.ThreadLocalRandom
import kotlin.jvm.optionals.getOrNull
import kotlin.math.pow

object SignatureManager : IonServerComponent(true) {
    private const val MAX_SPAWN_ATTEMPTS = 30
    private const val MIN_DISTANCE_FROM_STATIONS = 500

    val activeSignatures = mutableMapOf<Signature, Long>()

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

            Tasks.syncRepeat(60 * 20L, 60 * 20L) {
				tickSpawners()
				tickCurrentSignatures()
			}
        }
    }

    private fun tickSpawners() {
        for (signatureType in IonRegistries.SIGNATURE_TYPE.getAll()) {
            if (signatureType.isReadyToSpawn() && activeSignatures.count { signature -> signature.key.signatureType == signatureType } < signatureType.maximumPerServer) {
                val signature = generateNewSignature(signatureType) ?: continue
                log.info("Signature ${signature.signatureType.displayName.plainText()} spawned in ${signature.location.world}, ${signature.location.x}, ${signature.location.y}, ${signature.location.z}")
				IonServer.server.sendMessage(
					template(
						Component.text("Signature ${signature.signatureType.displayName.plainText()} spawned in ${signature.location.world}, ${signature.location.x}, ${signature.location.y}, ${signature.location.z}"),
						signature
					)
				)
				activeSignatures[signature] = System.nanoTime()
            }
        }
    }

	private fun tickCurrentSignatures() {
		val currentTime = System.nanoTime()

		activeSignatures.entries.removeIf { signature ->
			val maximumTime = signature.key.signatureType.despawnTime
			if (currentTime < signature.value + maximumTime) {
				//TODO: DESPAWN SCHEMATIC, maybe just remove valuable ores?
				true
			}
			else {
				false
			}
		}
	}

    private fun generateNewSignature(signatureType: SignatureType): Signature? {
        val location = getValidSignatureLocation() ?: return null
		val schematicFile: File = IonServer.dataFolder.resolve("signatures").resolve("${signatureType.schematicName}.schem")
		val clipboard: Clipboard = schematicCache[schematicFile].getOrNull() ?: return null
		createSignatureFromClipboard(log, location, clipboard)
        return Signature(signatureType, location)
    }

	fun createSignatureFromClipboard(
		logger: Logger,
		location: Location,
		clipboard: Clipboard,
	) {
		val target = StarshipDealers.resolveTarget(clipboard, location)
		val vec3i = Vec3i(target)
		logger.info("Attempting to place signature")
		placeSchematicEfficiently(clipboard, location.world, vec3i, true)
	}

    private fun getValidSignatureLocation(): Location? {
        var attempts = 0

        while (attempts < MAX_SPAWN_ATTEMPTS) {
            attempts += 1

            val spaceWorlds = Bukkit.getWorlds().filter { it.ion.hasFlag(WorldFlag.SPACE_WORLD) }
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

            val kothStationsInWorld = KothStationCache.stations.filter { station -> station.loc.world.name == randomSpaceWorld.name }
            for (kothStation in kothStationsInWorld) {
                if (distanceSquared(
                        kothStation.loc.x,
                        (randomSpaceWorld.maxHeight - randomSpaceWorld.minHeight).toDouble() / 2,
                        kothStation.loc.z,
                        potentialX,
                        potentialY,
                        potentialZ) <= (NATIONS_BALANCE.koths.majorKothradius + MIN_DISTANCE_FROM_STATIONS).toDouble().pow(2)
                ) continue
            }

            if (Regions.getAllOfInWorld<RegionFrontierTerritory>(randomSpaceWorld).any {
                territory -> territory.contains(potentialX.toInt(), potentialY.toInt(), potentialZ.toInt())
            }) continue

            return Location(randomSpaceWorld, potentialX, potentialY, potentialZ)
        }

        return null
    }
}
