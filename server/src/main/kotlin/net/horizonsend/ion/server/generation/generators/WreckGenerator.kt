package net.horizonsend.ion.server.generation.generators

import net.horizonsend.ion.server.NamespacedKeys
import net.starlegacy.util.Vec3i
import net.starlegacy.util.placeSchematicEfficiently
import net.starlegacy.util.readSchematic
import org.bukkit.Chunk
import org.bukkit.persistence.PersistentDataType
import java.io.File

object WreckGenerator {
	fun generateWreck(chunk: Chunk, x: Int, y: Int, z: Int, file: File) {
		val clipboard = readSchematic(file) ?: error("Failed to read ${file.path}")

		try {
			placeSchematicEfficiently(
				clipboard,
				chunk.world,
				Vec3i(x, y, z),
				true
			) {
				chunk.persistentDataContainer.set(
					NamespacedKeys.ASTEROIDS_WRECKS,
					PersistentDataType.STRING,
					file.nameWithoutExtension
				)
			}
		} catch (e: Exception) { e.printStackTrace() }
	}
}
