package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.core.registration.IonRegistries
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import org.bukkit.Bukkit
import org.bukkit.Location
import kotlin.random.Random

object SignatureManager : IonServerComponent(true) {

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
            if (signatureType.readyToSpawn() && activeSignatures.count { signature -> signature.signatureType == signatureType } < signatureType.maximumPerServer) {
                activeSignatures.add(generateNewSignature(signatureType))
            }
        }
    }

    private fun generateNewSignature(signatureType: SignatureType): Signature {
        val spaceWorlds = Bukkit.getWorlds().filter { it.ion.hasFlag(WorldFlag.SPACE_WORLD) }

        val randomSpaceWorld = spaceWorlds.random()
        val worldBorderMinX = randomSpaceWorld.worldBorder.center.x - (randomSpaceWorld.worldBorder.size / 2)
        val worldBorderMaxX = randomSpaceWorld.worldBorder.center.x + (randomSpaceWorld.worldBorder.size / 2)
        val worldBorderMinZ = randomSpaceWorld.worldBorder.center.z - (randomSpaceWorld.worldBorder.size / 2)
        val worldBorderMaxZ = randomSpaceWorld.worldBorder.center.z + (randomSpaceWorld.worldBorder.size / 2)

        val location = Location(randomSpaceWorld,
            Random.nextDouble(worldBorderMinX, worldBorderMaxX),
            (randomSpaceWorld.maxHeight - randomSpaceWorld.minHeight).toDouble() / 2,
            Random.nextDouble(worldBorderMinZ, worldBorderMaxZ)
        )

        return Signature(signatureType, location)
    }
}