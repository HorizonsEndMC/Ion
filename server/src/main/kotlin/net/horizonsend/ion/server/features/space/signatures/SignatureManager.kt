package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.common.utils.text.plainText
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.misc.CapturableStationCache
import net.horizonsend.ion.server.features.nations.NATIONS_BALANCE
import net.horizonsend.ion.server.features.space.spacestations.SpaceStationCache
import net.horizonsend.ion.server.features.starship.hyperspace.MassShadows
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.distanceSquared
import org.bukkit.Bukkit
import org.bukkit.Location
import java.util.concurrent.ThreadLocalRandom
import kotlin.math.pow

object SignatureManager : IonServerComponent(true) {
    private const val MAX_SPAWN_ATTEMPTS = 30
    private const val MIN_DISTANCE_FROM_STATIONS = 500

    val activeSignatures = mutableListOf<Signature>()

    override fun onEnable() {
        if (ConfigurationFiles.featureFlags().explorationSpawns) {
            for (signature in IonRegistries.SIGNATURE_TYPE.getAll()) {
                signature.calculateNewSpawnTime()
            }

            Tasks.syncRepeat(60 * 20L, 60 * 20L, SignatureManager::tickSpawners)
        }
    }

    private fun tickSpawners() {
        for (signatureType in IonRegistries.SIGNATURE_TYPE.getAll()) {
            if (signatureType.isReadyToSpawn() && activeSignatures.count { signature -> signature.signatureType == signatureType } < signatureType.maximumPerServer) {
                val signature = generateNewSignature(signatureType) ?: continue
                log.info("Signature ${signature.signatureType.displayName.plainText()} spawned in ${signature.location.world}, ${signature.location.x}, ${signature.location.y}, ${signature.location.z}")
                activeSignatures.add(signature)
            }
        }
    }

    private fun generateNewSignature(signatureType: SignatureType): Signature? {
        val location = getValidSignatureLocation() ?: return null
        return Signature(signatureType, location)
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
            if (massShadows != null && !massShadows.isEmpty()) continue

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