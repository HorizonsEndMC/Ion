package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

interface TickedMultiblockEntityParent {
	val tickInterval: Int
	var currentTick: Int

	var sleepTicks: Int

	/**
	 * Returns true if the current tick should be allowed to proceed
	 **/
	fun checkTickInterval(): Boolean {
		if (sleepTicks >= 1) {
			sleepTicks--

			return false
		}

		currentTick++
		if (currentTick >= tickInterval) {
			currentTick = 0
			return false
		}

		return true
	}

	/**
	 *
	 **/
	fun shouldCheckIntegrity(): Boolean = true
}
