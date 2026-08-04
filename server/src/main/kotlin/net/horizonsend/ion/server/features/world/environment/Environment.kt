package net.horizonsend.ion.server.features.world.environment

import org.bukkit.World

enum class Environment {

	;

	protected fun World.hasEnvironment(): Boolean = false // this.ion.environments.contains(this@Environment)
}
