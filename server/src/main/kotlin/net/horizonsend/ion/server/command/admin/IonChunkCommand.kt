package net.horizonsend.ion.server.command.admin

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.world.chunk.IonChunk.Companion.ion
import org.bukkit.entity.Player

@CommandAlias("ionchunk")
object IonChunkCommand : SLCommand() {
	@Subcommand("dumpchunk")
	fun dumpChunk(sender: Player) {
		val ionChunk = sender.chunk.ion()

		sender.information("Chunk: $ionChunk")
	}
}
