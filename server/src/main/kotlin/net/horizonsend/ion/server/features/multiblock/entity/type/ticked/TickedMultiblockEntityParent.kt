package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

import java.time.Duration

interface TickedMultiblockEntityParent {
	val tickingManager: TickingManager

	/**
	 * Whether to check multiblock integrity before ticking
	 **/
	fun shouldCheckIntegrity(): Boolean = true

	class TickingManager(val interval: Int) {
		private var currentTick: Int = 0

		// Store the sleep tick end as an epoch milli to make it independent of tick rate
		private var sleepTicksEnd: Long = 0

		/**
		 * Returns true if the current tick should be allowed to proceed
		 **/
		fun checkTickInterval(): Boolean {
			if (System.currentTimeMillis() < sleepTicksEnd) return false

			currentTick++

			if (currentTick < interval) {
				return false
			}

			currentTick = 0

			return true
		}

		fun sleepForTicks(ticks: Int) {
			sleepTicksEnd = System.currentTimeMillis() + (ticks * 50)
		}

		fun sleep(duration: Duration) {
			sleepTicksEnd = System.currentTimeMillis() + duration.toMillis()
		}

		fun sleepUntil(time: Long) {
			sleepTicksEnd = time
		}

		fun clearSleep() {
			sleepTicksEnd = 0
		}
	}
}
