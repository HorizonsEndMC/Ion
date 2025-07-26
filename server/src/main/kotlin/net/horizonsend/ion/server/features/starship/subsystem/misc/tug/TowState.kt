package net.horizonsend.ion.server.features.starship.subsystem.misc.tug

import net.horizonsend.ion.server.features.starship.movement.TransformationAccessor
import java.util.concurrent.Future

sealed interface TowState {
	fun canStartDiscovery(): Boolean
	fun canMove(): Boolean = canStartDiscovery()

	fun handleMovement(transformationAccessor: TransformationAccessor) {}

	data class Full(val blocks: TowedBlocks) : TowState {
		override fun canStartDiscovery(): Boolean = true

		override fun handleMovement(transformationAccessor: TransformationAccessor) {
			blocks.move(transformationAccessor)
		}
	}

	data class Discovering(val future: Future<TowedBlocks?>, val previous: TowState) : TowState {
		override fun canStartDiscovery(): Boolean = false
	}

	data object Empty : TowState {
		override fun canStartDiscovery(): Boolean = true
	}
}
