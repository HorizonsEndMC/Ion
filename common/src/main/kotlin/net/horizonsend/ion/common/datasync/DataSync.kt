package net.horizonsend.ion.common.datasync

import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import net.horizonsend.ion.common.IonComponent
import net.horizonsend.ion.common.utils.DBVec3i

data class Ship(val name: String, val pilot: String?, val type: String, val blocks: Int, val worldName: String)
data class Station(val name: String, val location: DBVec3i, val world: String, val owner: String?, val time: Int)
data class SyncData(
	val listShips: List<Ship>,
	val onlinePlayers: List<String>,
	val currentStation: Station,
	val allStations: List<Station>,
	val tps: Double
)

class DataSync(private val isSurvival: Boolean, private val supplier: (() -> SyncData)?) : IonComponent() {
	companion object {
		val data get() = backingObject
		private var backingObject = SyncData(
			emptyList(),
			emptyList(),
			Station("data-not-loaded", DBVec3i(0, 0, 0), "data-not-loaded", "data-not-loaded", -1),
			emptyList(),
			-1.0
		)
	}

	val syncAction by lazy {
		{ data: SyncData ->
			if (!isSurvival) {
				backingObject = data
			}
		}.registerRedisAction("sync-data", true)
	}

	@OptIn(DelicateCoroutinesApi::class)
	override fun onEnable() {
		syncAction // init lazy, so this gets called after everything is done initializing
		if (!isSurvival) return

		GlobalScope.launch {
			while (true) {
				delay(100L)
				syncAction(supplier!!())
			}
		}
	}
}
