package net.horizonsend.ion.server.features.space.generation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.extensions.information
import net.horizonsend.ion.server.extensions.serverError
import net.horizonsend.ion.server.extensions.success
import net.horizonsend.ion.server.extensions.userError
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
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

				try {
					SpaceGenerator.rebuildChunkAsteroids(chunk2)
				} catch (error: java.lang.Error) {
					error.printStackTrace()
					error.message?.let { sender.serverError(it) }
					continue
				}
			}
		}
		sender.success("Success!")
	}

	@Suppress("unused")
	@Subcommand("create asteroid")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, @Optional size: Double?, @Optional index: Int?, @Optional octaves: Int?) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		try {
			val asteroid = generator.generateWorldAsteroid(
				sender.location.blockX,
				sender.location.blockY,
				sender.location.blockZ,
				size,
				index,
				octaves
			)

			generator.generateAsteroid(asteroid)
		} catch (err: java.lang.Exception) {
			sender.serverError(err.message ?: "Error generating asteroid")
			err.printStackTrace()
			return
		}

		sender.success("Success!")
	}

	@Suppress("unused")
	@Subcommand("create wreck")
	@CommandCompletion("@wreckSchematics @wreckEncounters")
	fun onGenerateWreck(sender: Player, @Optional wreck: String?, @Optional encounter: String?) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		val data = wreck?.let { wreckName ->
			encounter?.let { encounterName ->

				SpaceGenerator.WreckGenerationData.WreckEncounterData(
					encounterName, null
				)
			}

			SpaceGenerator.WreckGenerationData(
				sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt(), wreckName, null
			)
		} ?: generator.generateRandomWreckData(sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt())

		generator.generateWreck(data)
	}
}
