package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.transport.filters.FilterBlock.Companion.FILTER_MATERIAL
import net.horizonsend.ion.server.features.transport.filters.FilterBlock.Companion.FILTER_STATE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.block.Barrel
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

object FilterAccess : IonServerComponent() {
	@EventHandler(priority = EventPriority.NORMAL)
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val interactedWith = event.item ?: return
		if (interactedWith.customItem !is Wrench) return

		val clicked = event.clickedBlock ?: return
		val state = clicked.state

		if (!FILTER_STATE_TYPE.isInstance(state)) return
		state as Barrel

		val type = FilterBlocks.getFilterBlock(state) ?: return
		val filterData = state.persistentDataContainer.get(NamespacedKeys.FILTER_DATA, FilterData) ?: return

		type.openGUI(event.player, filterData)
	}

	@EventHandler
	fun onBlockBreak(event: BlockBreakEvent) {
		if (event.block.type != FILTER_MATERIAL) return

		val state = event.block.state
		if (state !is Barrel) return

		val filterType = FilterBlocks.getFilterBlock(state) ?: return
		event.isDropItems = false

	}
}
