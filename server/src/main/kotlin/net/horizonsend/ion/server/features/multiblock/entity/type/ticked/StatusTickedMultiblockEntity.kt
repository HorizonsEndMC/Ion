package net.horizonsend.ion.server.features.multiblock.entity.type.ticked

import net.horizonsend.ion.server.features.multiblock.entity.type.StatusMultiblockEntity
import net.kyori.adventure.text.Component

/** Meant to be used with independently implemented ticked interface, just provides a util function */
interface StatusTickedMultiblockEntity : TickedMultiblockEntityParent, StatusMultiblockEntity {
	fun sleepWithStatus(status: Component, sleepTicks: Int) {
		setStatus(status)
		tickingManager.sleep(sleepTicks)
	}
}
