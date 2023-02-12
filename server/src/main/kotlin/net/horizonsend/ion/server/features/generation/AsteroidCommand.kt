package net.horizonsend.ion.server.commands

import co.aikar.commands.BaseCommand
import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.server.configuration.ServerConfiguration
import net.horizonsend.ion.server.extensions.sendInformation
import net.horizonsend.ion.server.extensions.sendServerError
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.generateRandomAsteroid
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator.postGenerateAsteroid
import net.horizonsend.ion.server.legacy.feedback.FeedbackType
import net.horizonsend.ion.server.legacy.feedback.sendFeedbackMessage
import net.minecraft.world.level.ChunkPos
import net.starlegacy.util.Tasks
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.entity.Player
import java.util.Random
import kotlin.math.sin

@CommandAlias("asteroid")
class AsteroidCommand(val configuration: ServerConfiguration) : BaseCommand() {
	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
	@CommandCompletion("Range")
	@Subcommand("regenerate")
	fun onRegenerate(sender: Player, @Optional @Default("0") range: Int) {
		sender.sendInformation("Regenerating")
		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				val chunk2 = sender.world.getChunkAt(x, z)

				try {
					AsteroidGenerator.rebuildChunkAsteroids(chunk2)
				} catch (error: java.lang.Error) {
					error.printStackTrace()
					error.message?.let { sender.sendServerError(it) }
					continue
				}
			}
		}
		sender.sendInformation("Success!")
	}

	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
	@Subcommand("create custom")
	@CommandCompletion("size index octaves")
	fun onCreateCustom(sender: Player, size: Double, index: Int, octaves: Int) {
		if (!IntRange(0, configuration.asteroidConfig.blockPalettes.size).contains(index)) {
			sender.sendFeedbackMessage(FeedbackType.USER_ERROR, "ERROR: index out of range: 0..${configuration.asteroidConfig.blockPalettes.size - 1}")
			return
		}

		val world = sender.world as CraftWorld

		val asteroid = AsteroidGenerator.Asteroid(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			configuration.asteroidConfig.blockPalettes[index],
			size,
			octaves
		)

		try {
			postGenerateAsteroid(world.handle, asteroid)
		} catch (err: java.lang.Exception) {
			sender.sendServerError(err.message ?: "Error generating asteroid")
			err.printStackTrace()
			return
		}

		sender.sendInformation("Success!")
	}

	@Suppress("unused")
	@CommandPermission("ion.space.regenerate")
	@Subcommand("create random")
	fun onCreateRandom(sender: Player) {
		val chunkPos = ChunkPos(sender.chunk.x, sender.chunk.z)
		val world = sender.world as CraftWorld

		val asteroidRandom = Random((chunkPos.x * 9999991) + sin(chunkPos.z.toDouble()).toLong() + world.seed)

		val asteroid = generateRandomAsteroid(
			sender.location.x.toInt(),
			sender.location.y.toInt(),
			sender.location.z.toInt(),
			asteroidRandom
		)

		Tasks.async {
			postGenerateAsteroid(
				world.handle,
				asteroid
			)
		}

		sender.sendInformation("Success!")
	}
}
