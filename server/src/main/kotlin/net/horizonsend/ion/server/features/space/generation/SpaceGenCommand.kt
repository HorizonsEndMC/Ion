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
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.server.features.space.generation.generators.GenerateAsteroidTask
import net.horizonsend.ion.server.features.space.generation.generators.GenerateWreckTask
import net.horizonsend.ion.server.features.space.generation.generators.SpaceGenerator
import net.horizonsend.ion.server.miscellaneous.NamespacedKeys
import net.minecraft.nbt.NbtUtils
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
			sender.location.blockX,
			sender.location.blockY,
			sender.location.blockZ,
			size,
			index,
			octaves
		)

		SpaceGenerationManager.generateFeature(GenerateAsteroidTask(generator, asteroid))

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
			encounter?.let { encounterName ->

				SpaceGenerator.WreckGenerationData.WreckEncounterData(
					encounterName, null
				)
			}

			SpaceGenerator.WreckGenerationData(
				sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt(), wreckName, null
			)
		} ?: generator.generateRandomWreckData(sender.location.x.toInt(), sender.location.y.toInt(), sender.location.z.toInt())

		SpaceGenerationManager.generateFeature(GenerateWreckTask(generator, data))
		sender.success("Success! Generated wreck ${data.schematicName} with encounter ${data.encounter}")
	}

	@Suppress("unused")
	@Subcommand("get")
	@CommandCompletion("WRECK_ENCOUNTER_DATA|STORED_CHUNK_BLOCKS")
	fun get(sender: Player, namespacedKey: String) {
		val chunk = sender.world.getChunkAt(sender.location)

		val key = when (namespacedKey) {
			"WRECK_ENCOUNTER_DATA" -> NamespacedKeys.WRECK_ENCOUNTER_DATA
			"STORED_CHUNK_BLOCKS" -> NamespacedKeys.STORED_CHUNK_BLOCKS
			else -> return sender.userError("No data found")
		}

		val data = BlockSerialization.readChunkCompoundTag(chunk, key)
		data?.let {
			val snbt = NbtUtils.structureToSnbt(it)
			sender.information(snbt)
		} ?: sender.userError("No data found")
	}
}
