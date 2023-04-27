package net.horizonsend.ion.server.features.space.generation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.features.space.data.StoredChunkBlocks
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.STORED_CHUNK_BLOCKS
import org.bukkit.entity.Player

@CommandPermission("ion.spacegen")
@CommandAlias("spacegen")
class SpaceGenCommand : BaseCommand() {
	@Suppress("unused")
	@CommandCompletion("Range")
	@Subcommand("regenerate")
	fun onRegenerate(sender: Player, @Optional @Default("0") range: Int) {
		sender.information("Regenerating")
		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				val chunk2 = sender.world.getChunkAt(x, z)

				println(chunk2.persistentDataContainer.get(STORED_CHUNK_BLOCKS, StoredChunkBlocks))

				try {
					SpaceGenerator.regenerateChunk(chunk2)
				} catch (error: java.lang.Error) {
					error.printStackTrace()
					error.message?.let { sender.serverError(it) }
					continue
				}
			}
		}

		sender.success("Success! Regenerated all chunks in a $range chunk radius")
	}
}
