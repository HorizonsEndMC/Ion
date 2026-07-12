package net.horizonsend.ion.server.listener.misc

import com.fastasyncworldedit.core.queue.IBatchProcessor
import com.fastasyncworldedit.core.queue.IChunk
import com.fastasyncworldedit.core.queue.IChunkGet
import com.fastasyncworldedit.core.queue.IChunkSet
import com.sk89q.worldedit.EditSession
import com.sk89q.worldedit.WorldEdit
import com.sk89q.worldedit.event.Event
import com.sk89q.worldedit.event.extent.EditSessionEvent
import com.sk89q.worldedit.extent.Extent
import com.sk89q.worldedit.util.eventbus.Subscribe
import net.horizonsend.ion.server.core.IonServerComponent
import net.horizonsend.ion.server.features.transport.util.IonChangeSet
import org.bukkit.Bukkit

private object ChiseledBookshelfTileSanitiser : IBatchProcessor {
	override fun processSet(chunk: IChunk, get: IChunkGet, set: IChunkSet): IChunkSet {
		val iterator = set.tiles().entries.iterator()

		while (iterator.hasNext()) {
			val tile = iterator.next().value
			val tileId = tile.linTag().value()["id"]?.value()

			if (tileId == "minecraft:chiseled_bookshelf") {
				iterator.remove()
			}
		}

		return set
	}

	override fun construct(extent: Extent): Extent = extent
}

object WorldEditListener : IonServerComponent(true) {
	override fun onEnable() {
		if (!Bukkit.getPluginManager().isPluginEnabled("FastAsyncWorldEdit")) return
		val worldEdit = runCatching { WorldEdit.getInstance() }.getOrElse { return }

		registerEvents(worldEdit)
	}

	private fun registerEvents(worldEdit: WorldEdit) {
		registerListener<EditSessionEvent>(worldEdit) { event ->
			if (event.stage != EditSession.Stage.BEFORE_HISTORY) return@registerListener

			var extent = event.extent.addProcessor(ChiseledBookshelfTileSanitiser)
			event.world?.let { extent = extent.addPostProcessor(IonChangeSet(it)) }
			event.setExtent(extent)
		}
	}

	private inline fun <reified T: Event> registerListener(worldEdit: WorldEdit, crossinline block: (T) -> Unit) {
		worldEdit.eventBus.register(object {
			val eventClass = T::class.java

			@Subscribe
			@Suppress("unused") // entrypoint
			fun onReceiveEvent(event: T) {
				if (eventClass.isInstance(event)) block.invoke(event as? T ?: return)
			}
		})
	}
}
