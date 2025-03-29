package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.misc.MultiblockCommand
import net.horizonsend.ion.server.features.custom.blocks.CustomBlock
import net.horizonsend.ion.server.features.custom.blocks.CustomBlockListeners
import net.horizonsend.ion.server.features.custom.blocks.CustomBlocks
import net.horizonsend.ion.server.features.custom.blocks.misc.WrenchRemovable
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.multiblock.MultiblockAccess
import net.horizonsend.ion.server.features.multiblock.PrePackaged
import net.horizonsend.ion.server.miscellaneous.utils.isWallSign
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Block
import org.bukkit.block.Sign
import org.bukkit.entity.Player
import org.bukkit.event.block.BlockBreakEvent
import org.bukkit.event.player.PlayerInteractEvent

object Wrench : CustomItem(
	"WRENCH",
	text("Wrench"),
	ItemFactory.builder(ItemFactory.unStackableCustomItem)
		.setCustomModel("tool/wrench")
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@Wrench) { event, _, itemStack ->
			handleRightClick(event.player, event)
		})

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@Wrench) { event, _, itemStack ->
			checkStructure(event.player, event)
		})
	}

	private fun checkStructure(player: Player, event: PlayerInteractEvent) {
		val hitBlock = event.clickedBlock
		if (hitBlock?.type?.isWallSign != true) return

		val sign = hitBlock.state as? Sign ?: return
		val multiblock = MultiblockAccess.getFast(sign) ?: return

		if (multiblock.signMatchesStructure(sign, loadChunks = false, particles =  false)) {
			player.information("Multiblock structure is correct.")
			return
		}

		MultiblockCommand.onCheck(player, multiblock, sign.x, sign.y, sign.z)
	}

	private fun handleRightClick(player: Player, event: PlayerInteractEvent?) {
		val clickedBlock = event?.clickedBlock ?: return
		val state = clickedBlock.state
		val customBlock = CustomBlocks.getByBlockData(clickedBlock.blockData)

		if (player.isSneaking && state is Sign) return tryPickUpMultiblock(player, state)
		if (player.isSneaking && customBlock is WrenchRemovable) return tryPickUpBlock(player, clickedBlock, customBlock)
	}

	private fun tryPickUpMultiblock(player: Player, sign: Sign) {
		PrePackaged.pickUpStructure(player, sign)
	}

	private fun tryPickUpBlock(player: Player, block: Block, customBlock: WrenchRemovable) {
		val event = BlockBreakEvent(block, player)
		CustomBlockListeners.noDropEvents.add(event)

		if (!event.callEvent()) return

		val item = (customBlock as CustomBlock).customItem.constructItemStack()
		customBlock.decorateItem(item, block)

		block.type = Material.AIR
		block.world.dropItem(
			block.location.toCenterLocation(),
			item
		)
	}
}
