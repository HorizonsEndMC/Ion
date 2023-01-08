package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.ConditionFailedException
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.loadConfiguration
import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.NamespacedKeys
import net.horizonsend.ion.server.generation.Asteroid
import net.horizonsend.ion.server.generation.AsteroidsDataType
import net.horizonsend.ion.server.generation.PlacedOre
import net.horizonsend.ion.server.generation.PlacedOresDataType
import net.horizonsend.ion.server.generation.configuration.AsteroidConfiguration
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.generateAsteroid
import net.horizonsend.ion.server.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.generation.generators.OreGenerator.generateOres
import net.horizonsend.ion.server.generation.generators.OreGenerator.getSphereBlocks
import net.minecraft.world.level.ChunkPos
import org.bukkit.World
import org.bukkit.entity.Player
import java.util.Random

@CommandAlias("asteroid")
class AsteroidCommand : BaseCommand() {
	private val configuration: AsteroidConfiguration =
		loadConfiguration(IonServer.Ion.dataFolder.resolve("asteroids"), "asteroid_configuration.conf")

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("regenerate asteroid")
	@CommandCompletion("Optional:Range")
	fun onRegenerateRangeAsteroid(sender: Player, range: Int = 0) {
		var placed = 0

		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				try {
					postGenerateAsteroids(sender.world, ChunkPos(x, z))
				} catch (error: ConditionFailedException) {
					sender.sendRichMessage("<red>${error.message}"); continue
				}

				placed += 1
			}
		}

		sender.sendRichMessage("<#7fff7f>Regenerated ores in $placed chunks!")
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
				generateOres(world, sender.chunk)

				chunkCount += 1
			}
		}

		sender.sendRichMessage("<#7fff7f>Success! Populated $chunkCount chunks with new ores!")
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
					postGenerateOres(sender.world, ChunkPos(x, z))
				} catch (error: ConditionFailedException) {
					sender.sendRichMessage("<red>${error.message}"); continue
				}

				placed += 1
			}
		}

		sender.sendRichMessage("<#7fff7f>Regenerated ores in $placed chunks!")
	}

	@Suppress("unused")
	@CommandPermission("spacegenerator.regenerate")
	@Subcommand("create custom")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, size: Double, index: Int, octaves: Int) {
		if (!IntRange(0, configuration.blockPalettes.size).contains(index)) {
			sender.sendRichMessage("<red>ERROR: index out of range: 0..${configuration.blockPalettes.size - 1}")
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

		run {
			postGenerateAsteroid(
				world,
				sender.chunk,
				asteroid
			)
		}

		sender.sendRichMessage("<#7fff7f>Success!")
	}

	@Suppress("unused")
	@Subcommand("create random")
	fun onCreateRandom(sender: Player) {
		val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
		val world = sender.world

		val asteroidRandom = Random(chunkPos.x + chunkPos.z + world.seed)

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

		sender.sendRichMessage("<#7fff7f>Success!")
	}

	private fun postGenerateAsteroids(world: World, chunkPos: ChunkPos) {
		val asteroids = getChunkAsteroids(world, chunkPos.x, chunkPos.z)

		if (asteroids.isEmpty()) {
			throw ConditionFailedException("No asteroids to regenerate for Chunk (${chunkPos.x}, ${chunkPos.z})!")
		}

		for (asteroid in asteroids) {
			postGenerateAsteroid(world, world.getChunkAt(chunkPos.x, chunkPos.z), asteroid)
		}
	}

	private fun postGenerateOres(world: World, chunkPos: ChunkPos) {
		val oreBlobs = getChunkOres(world, chunkPos.x, chunkPos.z)

		if (oreBlobs.isEmpty()) {
			throw ConditionFailedException("No ores to regenerate for Chunk (${chunkPos.x}, ${chunkPos.z})!")
		}

		for (ore in oreBlobs) {
			val oreBlocks = getSphereBlocks(ore.blobSize, origin = Triple(ore.x, ore.y, ore.z))

			for (block in oreBlocks) {
				ore.material.let { world.setBlockData(block.first, block.second, block.third, it) }
			}
		}
	}

	private fun getChunkAsteroids(world: World, chunkX: Int, chunkZ: Int): List<Asteroid> {
		val chunk = world.getChunkAt(chunkX, chunkZ)
		return chunk.persistentDataContainer.get(NamespacedKeys.ASTEROIDS, AsteroidsDataType())?.asteroids ?: listOf()
	}

	private fun getChunkOres(world: World, chunkX: Int, chunkZ: Int): List<PlacedOre> {
		val chunk = world.getChunkAt(chunkX, chunkZ)
		return chunk.persistentDataContainer.get(NamespacedKeys.ASTEROIDS_ORES, PlacedOresDataType())?.ores ?: listOf()
	}
}
