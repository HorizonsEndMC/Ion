package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

interface TickedMultiblockEntityParent {
	val tickingManager: TickingManager

	/**
	 *
	 **/
	fun shouldCheckIntegrity(): Boolean = true

	class TickingManager(val interval: Int) {
		private var currentTick: Int = 0
		private var sleepTicks: Int = 0

		/**
		 * Returns true if the current tick should be allowed to proceed
		 **/
		fun checkTickInterval(): Boolean {
			if (sleepTicks >= 1) {
				sleepTicks--

				return false
			}

			currentTick++
			if (currentTick >= interval) {
				currentTick = 0
				return false
			}

			return true
		}

		fun sleep(ticks: Int) {
			sleepTicks += ticks
		}

		fun clearSleep() {
			sleepTicks = 0
		}
	}
}
