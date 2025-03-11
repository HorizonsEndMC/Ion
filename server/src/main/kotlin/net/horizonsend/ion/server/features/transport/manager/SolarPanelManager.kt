package net.horizonsend.ion.server.features.transport.manager

import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap
import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet
import net.horizonsend.ion.server.features.transport.util.CombinedSolarPanel
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey
import org.bukkit.World
import java.lang.ref.WeakReference
import java.util.concurrent.locks.ReentrantReadWriteLock

abstract class SolarPanelManager() {
	private val solarPanelPositionMap = Long2ObjectOpenHashMap<WeakReference<CombinedSolarPanel>>()
	private val solarPanels = ObjectOpenHashSet<CombinedSolarPanel>()

	private val lock = ReentrantReadWriteLock()

	abstract fun getWorld(): World

	fun hasSolarPanel(position: BlockKey): Boolean {
		val contained = solarPanelPositionMap[position].get()
		if (contained == null) {
			solarPanelPositionMap.remove(position)
			solarPanels.trim()
			return false
		}

		return true
	}

	fun removePosition(position: BlockKey) {
		solarPanelPositionMap.remove(position)
	}

	fun removeSolarPanel(panel: CombinedSolarPanel) {
		solarPanels.remove(panel)
	}

	fun addSolarPanel(panel: CombinedSolarPanel) {
		panel.getPositions().associateWithTo(solarPanelPositionMap) { WeakReference(panel) }
		solarPanels.add(panel)
	}
}
