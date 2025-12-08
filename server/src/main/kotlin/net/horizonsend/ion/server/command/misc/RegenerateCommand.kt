package net.horizonsend.ion.server.command.misc

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import com.sk89q.worldedit.regions.Region
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.ores.storage.Ore
import net.horizonsend.ion.server.features.ores.storage.OreData
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getX
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getY
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.getZ
import net.horizonsend.ion.server.miscellaneous.utils.getSelection
import net.kyori.adventure.audience.Audience
import org.bukkit.World
import org.bukkit.entity.Player

@CommandPermission("ion.regenerate")
@CommandAlias("regenerate")
object RegenerateCommand : SLCommand() {
	@Subcommand("ores")
	fun onRegenerateOres(sender: Player) {
		val selection = sender.getSelection() ?: fail { "You must make a selection!" }

		regenerateOresInSelection(sender, selection, sender.world)
	}

	fun regenerateOresInSelection(feedback: Audience, region: Region, world: World) {
		feedback.information("Regenerating ores")
		val chunks = region.chunks
		val deferredChunks = chunks.map { pos ->
			val x = pos.x()
			val z = pos.z()

			world.getChunkAtAsync(x, z)
		}

		for (chunkFuture in deferredChunks) {
			chunkFuture.thenAccept { chunk -> runCatching {
				val stored = runCatching {
					chunk.persistentDataContainer.get(NamespacedKeys.ORE_DATA, OreData)
				}.onFailure {
					log.warn("Could not deserialize ore data!")
					it.printStackTrace()
				}.getOrNull() ?: return@thenAccept

				val toPlace = mutableMapOf<Vec3i, Ore>()

				for (i in 0 until stored.positions.size) {
					val key = stored.positions[i]
					val x = getX(key)
					val y = getY(key)
					val z = getZ(key)

					val oreIndex = stored.oreIndexes[i].toInt()
					val ore = stored.orePalette[oreIndex]

					toPlace[Vec3i(x, y, z)] = ore
				}

				Tasks.sync {
					for ((location, ore) in toPlace) {
						val (x, y, z) = location

						chunk.getBlock(x, y, z).setBlockData(ore.blockData, false)
					}

					feedback.information("Placed ${toPlace.size} ore blocks for chunk ${chunk.x} ${chunk.z}.")
				}
			}.onFailure {
				feedback.userError("Error regenerating! ${it.message}")
				it.printStackTrace()
			} }
		}
	}
}
