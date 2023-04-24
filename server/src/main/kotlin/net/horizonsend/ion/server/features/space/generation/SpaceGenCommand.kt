package net.horizonsend.ion.server.features.space.generation

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroidTask
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreckTask
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.features.space.generation.generators.WreckGenerationData
import net.horizonsend.ion.server.miscellaneous.minecraft
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.entity.Player
import java.util.Random

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

	@Suppress("unused")
	@Subcommand("create asteroid")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, @Optional size: Double?, @Optional index: Int?, @Optional octaves: Int?) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		val asteroid = generator.generateWorldAsteroid(
			sender.chunk.chunkKey,
			Random(sender.chunk.chunkKey),
			sender.world.minHeight,
			sender.world.maxHeight,
			sender.location.blockX,
			sender.location.blockY,
			sender.location.blockZ,
			size,
			index,
			octaves
		)

		SpaceGenerationManager.coroutineScope.launch {
			SpaceGenerationManager.postGenerateFeature(
				GenerateAsteroidTask(generator, sender.chunk.minecraft, listOf(asteroid)),
				SpaceGenerationManager.coroutineScope
			)
		}

		sender.success(
			"Success! Generated an asteroid of size ${asteroid.size} with palette" +
				" ${asteroid.palette.entries().map { it.bukkitMaterial }} and octaves ${asteroid.octaves}"
		)
	}

	@Suppress("unused")
	@Subcommand("create wreck")
	@CommandCompletion("@wreckSchematics @wreckEncounters")
	fun onGenerateWreck(sender: Player, @Optional wreck: String?, @Optional encounter: String?) {
		val generator = SpaceGenerationManager.getGenerator((sender.world as CraftWorld).handle) ?: return sender
			.userError("No generator found for ${sender.world.name}")

		val data = wreck?.let { wreckName ->
			val encounterData = encounter?.let { encounterName ->

				WreckGenerationData.WreckEncounterData(
					encounterName, null
				)
			}

			WreckGenerationData(
				sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt(), wreckName, encounterData
			)
		} ?: generator.generateRandomWreckData(sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt())

		SpaceGenerationManager.coroutineScope.launch {
			SpaceGenerationManager.postGenerateFeature(
				GenerateWreckTask(generator, sender.chunk.minecraft, listOf(data)),
				SpaceGenerationManager.coroutineScope
			)
		}

		sender.success("Success! Generated wreck ${data.wreckName} with encounter ${data.encounter}")
	}
}
