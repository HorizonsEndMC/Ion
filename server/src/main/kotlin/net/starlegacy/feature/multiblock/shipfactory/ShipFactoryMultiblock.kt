package net.starlegacy.feature.multiblock.shipfactory

import net.horizonsend.ion.server.miscellaneous.NamespacedKeys.SHIP_FACTORY_DATA
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor
import net.starlegacy.feature.multiblock.InteractableMultiblock
import net.starlegacy.feature.multiblock.MultiblockShape
import net.starlegacy.feature.multiblock.Multiblock
import net.starlegacy.feature.multiblock.PowerStoringMultiblock
import net.starlegacy.util.getFacing
import net.starlegacy.util.rightFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object ShipFactoryMultiblock : Multiblock(), PowerStoringMultiblock, InteractableMultiblock {
	override val name = "shipfactory"

	override val signText: Array<Component?> = arrayOf(
		text("Ship Factory", NamedTextColor.GOLD),
		null,
		null,
		null
	)

	override val maxPower: Int = 100_000

	override fun MultiblockShape.buildStructure() {
		z(+0) {
			y(-1) {
				x(+0).ironBlock()
				x(+1).ironBlock()
			}

			y(+0) {
				x(+0).furnace()
				x(+1).anyPipedInventory()
			}
		}
	}

	fun getStorage(sign: Sign): Inventory {
		val direction = sign.getFacing().oppositeFace
		return (sign.block.getRelative(direction).getRelative(direction.rightFace).state as InventoryHolder).inventory
	}

	override fun onSignInteract(sign: Sign, player: Player, event: PlayerInteractEvent) {
		val data = sign.persistentDataContainer.get(SHIP_FACTORY_DATA, ShipFactoryData)
		if (data == null) sign.persistentDataContainer.set(SHIP_FACTORY_DATA, ShipFactoryData, ShipFactoryData())

		ShipFactoryGUI(player, sign, data ?: ShipFactoryData()).show()
	}
}
