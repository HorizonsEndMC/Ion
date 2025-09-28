package net.horizonsend.ion.server.features.world.environment.modules

import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.environment.WorldEnvironmentManager
import net.horizonsend.ion.server.features.world.environment.listener.WrappedListener
import org.bukkit.World
import org.bukkit.entity.Player

abstract class EnvironmentModule(val manager: WorldEnvironmentManager) {
	protected val world get() = manager.world.world

	open fun tickSync() {}
	open fun tickAsync() {}

	open fun getWrappedListeners(): Collection<WrappedListener<*>> = listOf()

	private var wrappedListeners = getWrappedListeners()

	fun onUnload() {
		wrappedListeners.forEach(WrappedListener<*>::deRegister)
	}

	fun handleLoad() {
		wrappedListeners = getWrappedListeners()
		wrappedListeners.forEach(WrappedListener<*>::register)
	}

	open fun removeEffects(player: Player) {}

	protected fun World.hasEnvironment(): Boolean = this.ion.enviornmentManager.modules.contains(this@EnvironmentModule)
}
