package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.type.DisplayNameMultilblock
import net.horizonsend.ion.server.features.multiblock.type.EntityMultiblock
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

abstract class AbstractShipFactoryMultiblock <T: ShipFactoryEntity> : Multiblock(), InteractableMultiblock, DisplayNameMultilblock, EntityMultiblock<T> {
	override val name: String = "shipfactory"

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val entity = AdvancedShipFactoryMultiblock.getMultiblockEntity(sign) ?: return
		entity.openMenu(player)
	}
}
