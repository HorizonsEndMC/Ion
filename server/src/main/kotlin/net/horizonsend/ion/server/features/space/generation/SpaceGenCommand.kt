package net.horizonsend.ion.server.features.space.generation

import co.aikar.commands.annotation.CommandAlias
import co.aikar.commands.annotation.CommandCompletion
import co.aikar.commands.annotation.CommandPermission
import co.aikar.commands.annotation.Default
import co.aikar.commands.annotation.Optional
import co.aikar.commands.annotation.Subcommand
import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.common.extensions.serverError
import net.horizonsend.ion.common.extensions.success
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.command.SLCommand
import net.horizonsend.ion.server.features.space.data.CompletedSection
import net.horizonsend.ion.server.features.space.generation.generators.AsteroidGenerationData
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroid
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.miscellaneous.utils.Vec3i
import net.horizonsend.ion.server.miscellaneous.utils.minecraft
import org.bukkit.entity.Player
import java.util.Random
import java.util.concurrent.ThreadLocalRandom

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

	fun generateAsteroid(sender: Player, asteroidGenerationData: AsteroidGenerationData, generator: SpaceGenerator) {
		val range = (asteroidGenerationData.size.toInt()).shr(4)
		val sectionYOrigin = (asteroidGenerationData.y - sender.world.minHeight).shr(4)
		val random = ThreadLocalRandom.current()

		for (x in sender.chunk.x - range..sender.chunk.x + range) {
			for (z in sender.chunk.z - range..sender.chunk.z + range) {
				val chunk = sender.world.getChunkAt(x, z)
				val chunkMinX = chunk.x.shl(4)
				val chunkMinZ = chunk.z.shl(4)

				for (y in sectionYOrigin - range..sectionYOrigin + range) {
					val section = CompletedSection.empty(y)

					GenerateAsteroid.generateAsteroidSection(
						generator,
						asteroidGenerationData,
						section,
						y,
						chunkMinX,
						chunkMinZ,
						asteroidGenerationData.sizeFactor,
						random
					)

					try { section.place(chunk.minecraft) } catch (_: Throwable) { continue }
				}
			}
		}
	}

	@Suppress("unused")
	@Subcommand("generate asteroid manual")
	fun manualGenerateAsteroid(sender: Player, radius: Double, seed: Long, index: Int, octaves: Int) {
		val generator = SpaceGenerationManager.getGenerator(sender.world.minecraft) ?: return sender.userError("No space generator for ${sender.world.name}")
		sender.information("Generating asteroid")

		val (originX, originY, originZ) = Vec3i(sender.location)

		val data = generator.generateWorldAsteroid(
			chunkSeed = seed,
			chunkRandom = Random(Random(seed).nextLong()),
			sender.world.maxHeight,
			sender.world.minHeight,
			originX,
			originY,
			originZ,
			radius,
			index,
			octaves
		)

		generateAsteroid(
			sender,
			data,
			generator
		)
	}
}
