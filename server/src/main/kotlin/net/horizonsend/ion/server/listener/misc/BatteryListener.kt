package net.horizonsend.ion.server.listener.misc

import net.horizonsend.ion.server.features.gear.getPower
import net.horizonsend.ion.server.features.gear.setPower
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.entity.type.power.PoweredMultiblockEntity
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.listener.SLEventListener
import net.horizonsend.ion.server.miscellaneous.registrations.legacy.CustomItems
import org.bukkit.block.Sign
import org.bukkit.event.EventHandler
import org.bukkit.event.player.PlayerInteractEvent

object BatteryListener : SLEventListener() {
	@EventHandler
	fun onPlayerInteract(event: PlayerInteractEvent) {
		val sign = event.clickedBlock?.state as? Sign ?: return
		val multiblock = MultiblockAccess.getFast(sign) ?: return
		if (multiblock !is EntityMultiblock<*>) return

		val entity = multiblock.getMultiblockEntity(sign) ?: return
		if (entity !is PoweredMultiblockEntity) return

		val item = event.item ?: return
		if (CustomItems[item] !is CustomItems.BatteryItem) return

		handleBatteryInput(entity, event)
	}

	private fun handleBatteryInput(entity: PoweredMultiblockEntity, event: PlayerInteractEvent) {
		val item = event.item ?: return

		val power = getPower(item)
		var powerToTransfer = power * item.amount
		if (powerToTransfer == 0) return

		val machinePower = entity.powerStorage.getPower()
		val maxMachinePower = entity.powerStorage.capacity

		if (maxMachinePower - machinePower < powerToTransfer) {
			powerToTransfer = maxMachinePower - machinePower
		}

		setPower(item, power - powerToTransfer / item.amount)
		entity.powerStorage.addPower(powerToTransfer)
	}
}
