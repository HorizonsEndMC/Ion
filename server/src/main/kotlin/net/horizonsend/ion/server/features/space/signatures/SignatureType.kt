package net.horizonsend.ion.server.features.space.signatures

import com.google.common.cache.LoadingCache
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.extent.clipboard.Clipboard
import com.sk89q.worldedit.math.BlockVector3
import it.unimi.dsi.fastutil.longs.LongOpenHashSet
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.command.starship.BlueprintCommand
import net.horizonsend.ion.server.command.starship.BlueprintCommand.getPasteVector
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.Keyed
import net.horizonsend.ion.server.features.starship.active.ActiveStarship
import net.horizonsend.ion.server.features.starship.dealers.StarshipDealers
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.WeightedRandomList
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.placeSchematicEfficiently
import net.kyori.adventure.text.Component
import org.bukkit.Location
import org.bukkit.World
import java.io.File
import java.time.Duration
import java.util.Optional
import kotlin.jvm.optionals.getOrNull
import kotlin.random.Random

/** Causes the signature to persist on the map and despawn after a time limit. */
data class PersistentBehavior(
	val maximumPerServer: Int,
	val despawnTime: Duration,
)

/** Causes the signature to place a schematic on spawn. */
data class SchematicBehavior(
	val schematicNames: WeightedRandomList<String>,
	val callback: (LongOpenHashSet, World) -> Unit = { _, _ -> },
) {
	private var spawned = false

	fun generateSchematic(location: Location, cache: LoadingCache<File, Optional<Clipboard>>): Boolean {
		if (spawned) return false

		val schematicName = schematicNames.randomOrNull() ?: return false
		val schematicFile: File = IonServer.dataFolder.resolve("signatures").resolve("$schematicName.schem")
		val clipboard: Clipboard = cache[schematicFile].getOrNull() ?: return false

		// prevents the signature from spawning if it would replace another structure (uncomment this if someone's
		// base actually does get nuked)
		/*
		return Tasks.getSyncBlocking {
			if (isObstructed(location, clipboard, Vec3i(location.x.toInt(), location.y.toInt(), location.z.toInt()))) {
				return@getSyncBlocking false
			}

			val target = StarshipDealers.resolveTarget(clipboard, location)
			val vec3i = Vec3i(target)

			placeSchematicEfficiently(clipboard, location.world, vec3i, true) { placedBlocks ->
				callback.invoke(placedBlocks, location.world)
			}

			return@getSyncBlocking true
		}
		 */

		val target = StarshipDealers.resolveTarget(clipboard, location)
		val vec3i = Vec3i(target)

		placeSchematicEfficiently(clipboard, location.world, vec3i, true) { placedBlocks ->
			callback.invoke(placedBlocks, location.world)
		}

		spawned = true

		return true
	}

	fun isObstructed(location: Location, schematic: Clipboard, pilotLoc: Vec3i): Boolean {
		val world = BukkitAdapter.adapt(location.world)
		val vec: BlockVector3 = getPasteVector(location, pilotLoc)
		val region = schematic.region.clone()
		val offset = vec.subtract(schematic.origin)
		for (point in region) {
			if (!BlueprintCommand.isAir(schematic.getBlock(point)) && !BlueprintCommand.isAir(world.getBlock(point.add(offset)))) {
				return true
			}
		}

		return false
	}
}

/** Causes the signature to have a behavior when scanned by a probe. */
data class ScannableBehavior(
	val onScan: (Signature, ActiveStarship) -> Unit
)

class SignatureType(
	override val key: IonRegistryKey<SignatureType, out SignatureType>,
	val displayName: Component,
	val minSpawnTime: Duration,
	val maxSpawnTime: Duration,
	val persistentBehavior: PersistentBehavior? = null,
	val schematicBehavior: SchematicBehavior? = null,
	val scannableBehavior: ScannableBehavior? = null,
) : Keyed<SignatureType> {
	var nextSpawnTimeMillis: Long = 0L

	fun isReadyToSpawn(): Boolean {
		if (System.currentTimeMillis() <= nextSpawnTimeMillis) return false

		calculateNewSpawnTime()
		return true
	}

	fun calculateNewSpawnTime(): Long {
		nextSpawnTimeMillis = System.currentTimeMillis() + Random.nextLong(minSpawnTime.toMillis(), maxSpawnTime.toMillis())
		return nextSpawnTimeMillis
	}
}
