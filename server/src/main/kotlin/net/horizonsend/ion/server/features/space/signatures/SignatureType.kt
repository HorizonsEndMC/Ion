package net.horizonsend.ion.server.features.space.signatures

import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.extent.clipboard.Clipboard
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.miscellaneous.utils.ServerStage
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.slf4j.Logger
import java.io.File
import java.time.Duration
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

open class SignatureType(
	override val key: IonRegistryKey<SignatureType, out SignatureType>,
	val displayName: Component,
	val minSpawnTimeMinutes: Duration,
	val maxSpawnTimeMinutes: Duration,
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

	open fun onSpawn() { }
}

class PersistentSignatureType(
	key: IonRegistryKey<SignatureType, out SignatureType>,
	displayName: Component,
	minSpawnTimeMinutes: Duration,
	maxSpawnTimeMinutes: Duration,
	minimumServerStage: Int = 0,

	val maximumPerServer: Int,
	val detectionRange: Int,
	val interactRange: Int,
	val despawnTimeMinutes: Duration,
) : SignatureType(key, displayName, minSpawnTimeMinutes, maxSpawnTimeMinutes, minimumServerStage)

class SchematicSignatureType(
	key: IonRegistryKey<SignatureType, out SignatureType>,
	displayName: Component,
	minSpawnTimeMinutes: Duration,
	maxSpawnTimeMinutes: Duration,
	minimumServerStage: Int = 0,

	val schematicNames: WeightedRandomList<String>,
) : SignatureType(key, displayName, minSpawnTimeMinutes, maxSpawnTimeMinutes, minimumServerStage) {

	fun generateSchematic(location: Location, cache: LoadingCache<File, Optional<Clipboard>>, logger: Logger) : Boolean {
		val schematicName = schematicNames.randomOrNull() ?: return false
		val schematicFile: File = IonServer.dataFolder.resolve("signatures").resolve("$schematicName.schem")
		val clipboard: Clipboard = cache[schematicFile].getOrNull() ?: return false

		val target = StarshipDealers.resolveTarget(clipboard, location)
		val vec3i = Vec3i(target)
		logger.info("Attempting to place signature schematic")
		placeSchematicEfficiently(clipboard, location.world, vec3i, true)

		return true
	}
}
