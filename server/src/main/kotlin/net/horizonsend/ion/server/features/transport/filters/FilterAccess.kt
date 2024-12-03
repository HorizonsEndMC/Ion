package net.horizonsend.ion.server.features.transport.filters

import net.horizonsend.ion.server.IonServerComponent
import net.horizonsend.ion.server.features.custom.items.CustomItems.customItem
import net.horizonsend.ion.server.features.custom.items.misc.Wrench
import net.horizonsend.ion.server.features.transport.filters.FilterBlock.Companion.FILTER_STATE_TYPE
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import org.bukkit.Material
import org.bukkit.block.Barrel
import org.bukkit.block.Block
import org.bukkit.event.EventHandler
import org.bukkit.event.EventPriority
import org.bukkit.event.player.PlayerInteractEvent

object FilterAccess : IonServerComponent() {
	@EventHandler(priority = EventPriority.NORMAL)
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val clicked = event.clickedBlock ?: return
		val state = clicked.state

		if (!FILTER_STATE_TYPE.isInstance(state)) return
		state as Barrel

		val type = FilterBlocks.getFilterBlock(state) ?: return

		val interactedWith = event.item ?: return
		if (interactedWith.customItem !is Wrench) {
			event.isCancelled = true
			return
		}

		if (event.player.isSneaking) {
			tryDrop(clicked, state, type)
			return
		}

		val filterData = state.persistentDataContainer.get(NamespacedKeys.FILTER_DATA, FilterData)

		type.openGUI(event.player, state, filterData)
	}

	private fun tryDrop(block: Block, state: Barrel, type: FilterBlock<*>) {
		val dropLocation = block.location.toCenterLocation()
		val item = type.customItem.createFor(state)

		block.type = Material.AIR

		block.world.dropItemNaturally(dropLocation, item)
	}
}
