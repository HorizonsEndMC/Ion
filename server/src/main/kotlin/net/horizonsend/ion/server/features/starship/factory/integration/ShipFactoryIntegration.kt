package net.horizonsend.ion.server.features.starship.factory.integration

import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.ShipFactoryEntity
import net.horizonsend.ion.server.features.starship.factory.PrintItem
import net.horizonsend.ion.server.features.starship.factory.ShipFactoryPrintTask
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.BlockKey

abstract class ShipFactoryIntegration<T : MultiblockEntity>(val taskEntity: ShipFactoryEntity, val integratedEntity: T) {
	/**
	 * Performs any setup tasks when the ship factory does
	 **/
	open fun asyncSetup(task: ShipFactoryPrintTask) {}

	/**
	 * Performs any setup tasks when the ship factory does
	 **/
	open fun syncSetup(task: ShipFactoryPrintTask) {}

	/**
	 * Sends a report of the integration's activities when the ship factory finishes
	 **/
	abstract fun sendReport(task: ShipFactoryPrintTask, hasFinished: Boolean)

	/**
	 * Starts a new transaction
	 **/
	abstract fun startNewTransaction(task: ShipFactoryPrintTask)

	/**
	 * Returns all block keys that failed the transaction
	 **/
	abstract fun commitTransaction(task: ShipFactoryPrintTask): List<BlockKey>

	abstract fun canAddTransaction(printItem: PrintItem, printPosition: BlockKey, requiredAmount: Int): Boolean
}
