package net.horizonsend.ion.server.features.generation

import net.horizonsend.ion.server.IonServer
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator
import net.minecraft.server.level.ServerLevel
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld

object SpaceGenerationManager {
	lateinit var worldGenerators: Map<ServerLevel, AsteroidGenerator?>

	fun onEnable() {
		worldGenerators = IonServer.Ion.server.worlds.associate { world ->
			val serverLevel = (world as CraftWorld).handle
			serverLevel to IonServer.Ion.configuration.spaceGenConfig[serverLevel.serverLevelData.levelName]?.let { config ->
				println(config)
				AsteroidGenerator(
					serverLevel,
					config
				)
			}
		}
	}

	fun getGenerator(serverLevel: ServerLevel): AsteroidGenerator? = worldGenerators[serverLevel]
}
