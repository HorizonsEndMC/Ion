package net.horizonsend.ion.server.features.world.generation.generators.bukkit

import org.bukkit.block.Biome
import org.bukkit.generator.BiomeProvider
import org.bukkit.generator.WorldInfo

open class SpaceBiomeProvider : BiomeProvider() {
	override fun getBiome(worldInfo: WorldInfo, x: Int, y: Int, z: Int) = Biome.THE_END
	override fun getBiomes(worldInfo: WorldInfo) = listOf(Biome.THE_END)
}
