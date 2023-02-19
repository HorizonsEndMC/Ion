package net.horizonsend.ion.server.features.space.generation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.extensions.information
import net.horizonsend.ion.server.extensions.serverError
import net.horizonsend.ion.server.extensions.success
import net.horizonsend.ion.server.extensions.userError
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.minecraft.world.level.ChunkPos
import net.starlegacy.util.Tasks
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.entity.Player
import java.util.Random
import kotlin.math.sin

@CommandAlias("asteroid")
class AsteroidCommand(val configuration: ServerConfiguration) : BaseCommand() {
	// TODO all these commands suck
	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
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
	@CommandPermission("ion.space.regenerate")
	@Subcommand("create custom")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, size: Double, index: Int, octaves: Int) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		if (!IntRange(0, generator.configuration.blockPalettes.size).contains(index)) {
			sender.userError("ERROR: index out of range: 0..${generator.configuration.blockPalettes.size - 1}")
			return
		}

		val asteroid = SpaceGenerator.AsteroidGenerationData(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			generator.weightedPalettes[index],
			size,
			octaves
		)

		try {
			generator.generateAsteroid(asteroid)
		} catch (err: java.lang.Exception) {
			sender.serverError(err.message ?: "Error generating asteroid")
			err.printStackTrace()
			return
		}

		sender.success("Success!")
	}

	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
	@Subcommand("create random")
	fun onCreateRandom(sender: Player) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
		val world = sender.world as CraftWorld

		val asteroidRandom = Random((chunkPos.x * 9999991) + sin(chunkPos.z.toDouble()).toLong() + world.seed)

		val asteroid = generator.generateRandomAsteroid(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			asteroidRandom
		)

		Tasks.async {
			generator.generateAsteroid(
				asteroid
			)
		}

		sender.success("Success!")
	}

	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
	@Subcommand("create wreck")
	@CommandCompletion("x|y|z|@wreckSchematics|@wreckEncounters")
	fun onGenerateWreck(sender: Player, @Optional wreck: String?, @Optional encounter: String?) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		val data = wreck?.let { wreckName ->
			encounter?.let { encounterName ->
				// val mappedAdditionalInfo = generator.configuration.mappedAdditionalInfo

				SpaceGenerator.WreckGenerationData.WreckEncounterData(
					encounterName, null
					// mappedAdditionalInfo[encounterName + wreckName]
				)
			}

			SpaceGenerator.WreckGenerationData(
				sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt(), wreckName, null
			)
		} ?: generator.generateRandomWreckData(sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt())

		generator.generateWreck(data)
	}
}
