package net.horizonsend.ion.server.command.space

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.world.generation.regenerateChunk
import org.bukkit.entity.Player

@CommandPermission("ion.spacegen")
@CommandAlias("spacegen")
object SpaceGenCommand : SLCommand() {
	@Suppress("unused")
	@CommandCompletion("Range")
	@Subcommand("regenerate")
	fun onRegenerate(sender: Player, @Optional @Default("0") range: Int) {
		sender.information("Regenerating")
		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				val chunk2 = sender.world.getChunkAt(x, z)

				try {
					regenerateChunk(chunk2)
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
