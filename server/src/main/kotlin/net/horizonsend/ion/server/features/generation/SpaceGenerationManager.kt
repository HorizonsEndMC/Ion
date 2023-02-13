package net.horizonsend.ion.server.features.generation

import net.horizonsend.ion.server.IonServer.Companion.Ion
import net.horizonsend.ion.server.features.generation.generators.AsteroidGenerator
import net.minecraft.server.level.ServerLevel
import org.bukkit.craftbukkit.v1_19_R2.CraftWorld
import org.bukkit.event.EventHandler
import org.bukkit.event.Listener
import org.bukkit.event.world.WorldInitEvent

object SpaceGenerationManager : Listener {
	val worldGenerators: MutableMap<ServerLevel, AsteroidGenerator?> = mutableMapOf()

	fun getGenerator(serverLevel: ServerLevel): AsteroidGenerator? = worldGenerators[serverLevel]

	@EventHandler
	fun onWorldInit(event: WorldInitEvent) {
		val serverLevel = (event.world as CraftWorld).handle

		Ion.configuration.spaceGenConfig[event.world.name]?.let { config ->
			worldGenerators[serverLevel] =
				AsteroidGenerator(
					serverLevel,
					config
				)
		}
	}
}
