package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.NamespacedKeys
import net.horizonsend.ion.server.ServerConfiguration
import net.horizonsend.ion.server.generation.Asteroid
import net.horizonsend.ion.server.generation.AsteroidsDataType
import net.horizonsend.ion.server.generation.PlacedOre
import net.horizonsend.ion.server.generation.PlacedOresDataType
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.generateAsteroid
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.generation.generators.OreGenerator.generateOre
import net.horizonsend.ion.server.generation.generators.OreGenerator.generateOres
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.minecraft.world.level.ChunkPos
import org.bukkit.Chunk
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Random
import kotlin.math.sin

@CommandAlias("asteroid")
class AsteroidCommand(val configuration: ServerConfiguration) : BaseCommand() {
	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("regenerate asteroid")
	@CommandCompletion("Optional:Range")
	fun onRegenerateRangeAsteroid(sender: Player, range: Int = 0) {
		var placed = 0

		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				try {
					val chunk2 = sender.world.getChunkAt(x, z)
					postGenerateAsteroids(sender.world, chunk2)
				} catch (error: ConditionFailedException) {
					sender.sendFeedbackMessage(FeedbackType.SERVER_ERROR, "${error.message}"); continue
				}

				placed += 1
			}
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Regenerated ores in {0} chunks!", placed)
	}

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("create ore")
	@CommandCompletion("Optional:Range")
	fun onCreateOres(sender: Player, range: Int) {
		val world = sender.world

		var chunkCount = 0

		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				val chunk2 = world.getChunkAt(x, z)

				generateOres(world, chunk2)

				chunkCount += 1
			}
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Success! Populated {0} chunks with new ores!", chunkCount)
	}

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("regenerate ore")
	@CommandCompletion("Optional:Range")
	fun onRegenerateRangeOres(sender: Player, range: Int = 0) {
		var placed = 0

		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				try {
					val chunk2 = sender.world.getChunkAt(x, z)
					postGenerateOres(sender.world, chunk2)
				} catch (error: ConditionFailedException) {
					sender.sendFeedbackMessage(FeedbackType.SERVER_ERROR, "${error.message}"); continue
				}

				placed += 1
			}
		}

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Regenerated ores in {0} chunks!", placed)
	}

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("create custom")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, size: Double, index: Int, octaves: Int) {
		if (!IntRange(0, configuration.blockPalettes.size).contains(index)) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "ERROR: index out of range: 0..${configuration.blockPalettes.size - 1}")
			return
		}

		val world = sender.world

		val asteroid = Asteroid(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			configuration.blockPalettes[index],
			size,
			octaves
		)

		postGenerateAsteroid(
			world,
			sender.chunk,
			asteroid
		)

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Success!")
	}

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("create random")
	fun onCreateRandom(sender: Player) {
		val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
		val world = sender.world

		val asteroidRandom = Random((chunkPos.x * 9999991) + sin(chunkPos.z.toDouble()).toLong() + world.seed)

		val asteroid = generateAsteroid(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			asteroidRandom
		)

		postGenerateAsteroid(
			world,
			sender.chunk,
			asteroid
		)

		sender.sendFeedbackMessage(FeedbackType.SUCCESS, "Success!")
	}

	private fun postGenerateAsteroids(world: World, chunk: Chunk) {
		val asteroids = getChunkAsteroids(chunk)

		if (asteroids.isEmpty()) {
			throw ConditionFailedException("No asteroids to regenerate for Chunk (${chunk.x}, ${chunk.z})!")
		}

		for (asteroid in asteroids) {
			postGenerateAsteroid(world, chunk, asteroid)
		}
	}

	private fun postGenerateOres(world: World, chunk: Chunk) {
		val oreBlobs = getChunkOres(chunk)

		if (oreBlobs.isEmpty()) {
			throw ConditionFailedException("No ores to regenerate for Chunk (${chunk.x}, ${chunk.z})!")
		}

		for (ore in oreBlobs) {
			generateOre(world, ore)
		}
	}

	private fun getChunkAsteroids(chunk: Chunk): List<Asteroid> {
		return chunk.persistentDataContainer.get(NamespacedKeys.ASTEROIDS, AsteroidsDataType())?.asteroids ?: listOf()
	}

	private fun getChunkOres(chunk: Chunk): List<PlacedOre> {
		return chunk.persistentDataContainer.get(NamespacedKeys.ASTEROIDS_ORES, PlacedOresDataType())?.ores ?: listOf()
	}
}
