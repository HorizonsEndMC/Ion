package net.horizonsend.ion.server.features.world.generation.generators

import net.horizonsend.ion.server.features.world.IonWorld
import net.horizonsend.ion.server.features.world.generation.generators.configuration.generator.GenerationConfiguration
import net.minecraft.server.level.GenerationChunkHolder
import net.minecraft.util.StaticCache2D
import net.minecraft.world.level.LevelHeightAccessor
import net.minecraft.world.level.chunk.ChunkAccess
import net.minecraft.world.level.chunk.status.ChunkStep
import net.minecraft.world.level.chunk.status.WorldGenContext
import org.bukkit.generator.ChunkGenerator

abstract class IonWorldGenerator<T: GenerationConfiguration>(val world: IonWorld, val configuration: T) : ChunkGenerator() {
	val seed = world.world.seed
	val heightAccessor: LevelHeightAccessor = LevelHeightAccessor.create(world.world.minHeight, world.world.maxHeight - world.world.minHeight)

	open fun generateStructureStarts(context: WorldGenContext, step: ChunkStep, neighborCache: StaticCache2D<GenerationChunkHolder>, chunk: ChunkAccess) {}

	override fun isParallelCapable(): Boolean {
		return true
	}
}
