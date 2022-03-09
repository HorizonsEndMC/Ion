package net.horizonsend.ion

import org.bukkit.World
import org.spongepowered.configurate.objectmapping.ConfigSerializable

@ConfigSerializable
data class Configuration(
	val noSaveWorlds: Set<World> = setOf()
)
