package net.horizonsend.ion.server.features.space.signatures

import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.miscellaneous.utils.ServerStage
import net.kyori.adventure.text.Component
import java.time.Duration
import kotlin.random.Random

open class SignatureType(
    override val key: IonRegistryKey<SignatureType, out SignatureType>,
    val displayName: Component,
    val detectionRange: Int,
    val interactRange: Int,
    val maximumPerServer: Int,
    val minSpawnTimeMinutes: Duration,
    val maxSpawnTimeMinutes: Duration,
    val despawnTimeMinutes: Duration,
    val schematicName: String,
	val minimumServerStage: Int = 0,
	) : Keyed<SignatureType> {
    var nextSpawnTimeMillis: Long = 0L

    fun isReadyToSpawn(): Boolean {
        if (System.currentTimeMillis() <= nextSpawnTimeMillis) return false
		if (minimumServerStage > ServerStage.getServerStage()) return false

		calculateNewSpawnTime()
		return true
    }

    fun calculateNewSpawnTime(): Long {
        nextSpawnTimeMillis = System.currentTimeMillis() + Random.nextLong(minSpawnTimeMinutes.toMillis(), maxSpawnTimeMinutes.toMillis())
        return nextSpawnTimeMillis
    }
}
