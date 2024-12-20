package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.common.extensions.information
import net.horizonsend.ion.server.command.misc.MultiblockCommand
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
import org.bukkit.block.Sign
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent

object Wrench : CustomItem(
	"WRENCH",
	text("Wrench"),
	ItemFactory.unStackableCustomItem
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@Wrench) { event, _, itemStack ->
			handleSecondaryInteract(event.player, event)
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@Wrench) { event, _, itemStack ->
			handlePrimaryInteract(event.player, event)
		})
	}

	private fun handlePrimaryInteract(livingEntity: LivingEntity, event: PlayerInteractEvent) {
		if (livingEntity !is Player) return

		val hitBlock = event.clickedBlock
		if (hitBlock?.type?.isWallSign != true) return

		val sign = hitBlock.state as? Sign ?: return
		val multiblock = MultiblockAccess.getFast(sign) ?: return

		if (multiblock.signMatchesStructure(sign, loadChunks = false, particles =  false)) {
			livingEntity.information("Multiblock structure is correct.")
			return
		}

		MultiblockCommand.onCheck(livingEntity, multiblock, sign.x, sign.y, sign.z)
	}

	private fun handleSecondaryInteract(livingEntity: LivingEntity, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return
		val clickedBlock = event?.clickedBlock ?: return
		val state = clickedBlock.state

		if (livingEntity.isSneaking && state is Sign) return tryPickUpMultiblock(livingEntity, state)
	}

	private fun tryPickUpMultiblock(player: Player, sign: Sign) {
		PrePackaged.pickUpStructure(player, sign)
	}
}
