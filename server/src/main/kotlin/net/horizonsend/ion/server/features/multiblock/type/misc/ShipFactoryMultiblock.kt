package net.horizonsend.ion.server.features.multiblock.type.misc

import net.horizonsend.ion.server.features.multiblock.Multiblock
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.InteractableMultiblock
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object ShipFactoryMultiblock : Multiblock(), InteractableMultiblock {
	override val name = "shipfactory"

	override val signText = createSignText(
		line1 = "&1Ship Factory",
		line2 = null,
		line3 = null,
		line4 = null
	)

//	override val maxPower: Int = 1_000_000

	override fun onTransformSign(player: Player, sign: Sign) {
		sign.setLine(2, sign.getLine(1))
		sign.setLine(1, player.uniqueId.toString())
	}

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
		val leftClick = event.action == Action.LEFT_CLICK_BLOCK && event.player.hasPermission("starlegacy.factory.print.credit")

		Tasks.async {
			StarshipFactories.process(event.player, sign, leftClick)
		}
	}
}
