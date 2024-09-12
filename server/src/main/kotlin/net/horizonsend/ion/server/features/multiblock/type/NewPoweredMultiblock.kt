package net.horizonsend.ion.server.features.multiblock.type

import net.horizonsend.ion.server.features.gear.getPower
import net.horizonsend.ion.server.features.gear.setPower
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.entity.MultiblockEntity
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.listener.SLEventListener
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent

interface NewPoweredMultiblock<T : MultiblockEntity> : EntityMultiblock<T> {
	val maxPower: Int

	fun handleBatteryInput(sign: Sign, entity: PoweredMultiblockEntity, event: PlayerInteractEvent) {
		val item = event.item ?: return

		val power = getPower(item)
		var powerToTransfer = power * item.amount
		if (powerToTransfer == 0) return

		val machinePower = entity.storage.getPower()
		val maxMachinePower = entity.storage.capacity
		if (maxMachinePower - machinePower < powerToTransfer) {
			powerToTransfer = maxMachinePower - machinePower
		}

		setPower(item, power - powerToTransfer / item.amount)
		entity.storage.addPower(powerToTransfer)
	}

	companion object : SLEventListener() {
		@EventHandler
		fun onPlayerInteract(event: PlayerInteractEvent) {
			val sign = event.clickedBlock?.state as? Sign ?: return
			val multiblock = MultiblockAccess.getFast(sign) ?: return
			if (multiblock !is NewPoweredMultiblock<*>) return
			val entity = multiblock.getMultiblockEntity(sign) ?: return

			multiblock.handleBatteryInput(sign, entity as PoweredMultiblockEntity, event)
		}
	}
}
