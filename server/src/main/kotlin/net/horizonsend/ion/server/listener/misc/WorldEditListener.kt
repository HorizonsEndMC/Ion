package net.horizonsend.ion.server.listener.misc

import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.bukkit.BukkitAdapter
import com.sk89q.worldedit.event.Event
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.util.eventbus.Subscribe
import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.transport.IonUpdateExtent
import org.bukkit.Bukkit

object WorldEditListener : IonServerComponent(true) {
	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) return
		val worldEdit = runCatching { WorldEdit.getInstance() }.getOrElse { return }

		registerEvents(worldEdit)
	}

	private fun registerEvents(worldEdit: WorldEdit) {
		registerListener<EditSessionEvent>(worldEdit) { event ->
			if (event.stage != EditSession.Stage.BEFORE_HISTORY) return@registerListener

			println("Stage: ${event.stage}")

			val world = BukkitAdapter.adapt(event.world)
			println("Previous extent: ${event.extent}")
			println("Actor: ${event.actor}")
			println("event: $event")

			event.extent = IonUpdateExtent(event.extent, world, event.stage)
			println("after extent: ${event.extent}")
		}
	}

	private inline fun <reified T: Event> registerListener(worldEdit: WorldEdit, crossinline block: (T) -> Unit) {
		worldEdit.eventBus.register(object {
			val eventClass = T::class.java

			@Subscribe
			fun onReceiveEvent(event: T) {
				if (eventClass.isInstance(event)) block.invoke(event as? T ?: return)
			}
		})
	}
}
