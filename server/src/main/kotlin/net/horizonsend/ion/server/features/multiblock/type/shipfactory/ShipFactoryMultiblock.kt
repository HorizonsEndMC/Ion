package net.horizonsend.ion.server.features.multiblock.type.shipfactory

import net.horizonsend.ion.server.features.multiblock.entity.PersistentMultiblockData
import net.horizonsend.ion.server.features.multiblock.manager.MultiblockManager
import net.horizonsend.ion.server.features.multiblock.shape.MultiblockShape
import net.horizonsend.ion.server.features.multiblock.type.shipfactory.AdvancedShipFactoryMultiblock.AdvancedShipFactoryEntity
import net.horizonsend.ion.server.features.starship.factory.StarshipFactories
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.getFacing
import net.horizonsend.ion.server.miscellaneous.utils.rightFace
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import org.bukkit.World
import org.bukkit.block.BlockFace
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.Inventory
import org.bukkit.inventory.InventoryHolder

object ShipFactoryMultiblock : AbstractShipFactoryMultiblock<AdvancedShipFactoryEntity>() {

	override val signText = createSignText(
		line1 = "&1Ship Factory",
		line2 = null,
		line3 = null,
		line4 = null
	)

	override val displayName: Component get() = text("Ship Factory")
	override val description: Component get() = text("Print starships and other structures with materials and credits.")

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

	override fun createEntity(
		manager: MultiblockManager,
		data: PersistentMultiblockData,
		world: World,
		x: Int,
		y: Int,
		z: Int,
		structureDirection: BlockFace,
	): AdvancedShipFactoryEntity {
		return AdvancedShipFactoryEntity(data, manager, x, y, z, world, structureDirection)
	}
}
