package net.starlegacy.spacegenerator

import net.starlegacy.spacegenerator.asteroid.AsteroidData
import net.starlegacy.spacegenerator.asteroid.AsteroidOreType
import net.starlegacy.util.loadConfig
import org.bukkit.Bukkit
import org.bukkit.generator.ChunkGenerator
import org.bukkit.plugin.java.JavaPlugin
import java.io.File

class SpaceGenerator : JavaPlugin() {
    lateinit var config: SpaceGeneratorConfig

    override fun onLoad() {
        try {
            dataFolder.mkdirs()
            config = loadConfig(dataFolder, "generator_settings")
            AsteroidData.loadAsteroids(File(dataFolder, "asteroid_schematics"))
            validateConfig()
        } catch (exception: Throwable) {
            exception.printStackTrace()
            Bukkit.shutdown()
        }
    }

    private fun validateConfig() {
        config.worlds.forEach { (worldName, worldConfig) ->
            validateWorld(worldConfig, worldName)
        }
    }

    private fun validateWorld(worldConfig: SpaceGeneratorConfig.World, worldName: String) {
        validateRandomAsteroids(worldConfig, worldName)

        validateAsteroidBelts(worldConfig, worldName)
    }

    private fun validateRandomAsteroids(worldConfig: SpaceGeneratorConfig.World, worldName: String) {
        worldConfig.randomAsteroidOreDistribution.keys.forEach { AsteroidOreType.valueOf(it) }

        check(worldConfig.randomAsteroidOreDistribution.values
            .map { it.removeSuffix("%").toFloat() }
            .sum() <= 100)
        { "$worldName has asteroid ore percents adding up to more than 100%!" }

        for (asteroidName in worldConfig.randomAsteroids) {
            check(AsteroidData.cachedAsteroids.containsKey(asteroidName))
            { "Unknown asteroid $asteroidName specified as a random asteroid for $worldName not found" }
        }
    }

    private fun validateAsteroidBelts(worldConfig: SpaceGeneratorConfig.World, worldName: String) {
        for (asteroidBeltConfig in worldConfig.asteroidBelts) {
            asteroidBeltConfig.oreDistribution.keys.forEach { AsteroidOreType.valueOf(it) }
            check(asteroidBeltConfig.oreDistribution.values
                .map { it.removeSuffix("%").toFloat() }
                .sum() <= 100)
            { "$worldName's belt '${asteroidBeltConfig.description}' has asteroid ore percents adding up to more than 100%!" }

            for (asteroidName in asteroidBeltConfig.asteroids) {
                check(AsteroidData.cachedAsteroids.containsKey(asteroidName))
                { "Unknown asteroid $asteroidName specified as asteroid for belt ${asteroidBeltConfig.description} in $worldName not found" }
            }
        }
    }

    private val emptyWorldConfig = SpaceGeneratorConfig.World()

    override fun getDefaultWorldGenerator(worldName: String, id: String?): ChunkGenerator {
        val config = config.worlds[worldName] ?: emptyWorldConfig
        return SpaceChunkGenerator(config)
    }
}
