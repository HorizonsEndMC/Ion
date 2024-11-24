package net.horizonsend.ion.server.features.custom.items.misc

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.miscellaneous.utils.text.itemName
import net.horizonsend.ion.server.miscellaneous.utils.updateMeta
import net.kyori.adventure.text.Component.text
import org.bukkit.Material
import org.bukkit.block.Sign
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

object Wrench : CustomItem("WRENCH") {
	override fun constructItemStack(): ItemStack {
		return ItemStack(Material.WARPED_FUNGUS_ON_A_STICK).updateMeta {
			it.setCustomModelData(8000)
			it.displayName(text("Wrench").itemName)
		}
	}

	override fun handlePrimaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent) {

	}

	override fun handleSecondaryInteract(livingEntity: LivingEntity, itemStack: ItemStack, event: PlayerInteractEvent?) {
		if (livingEntity !is Player) return
		val clickedBlock = event?.clickedBlock ?: return
		val state = clickedBlock.state

		if (livingEntity.isSneaking && state is Sign) return tryPickUpMultiblock(livingEntity, state)
		if (state is Sign) return tryEditFilter(livingEntity, state)
	}

	private fun tryPickUpMultiblock(player: Player, sign: Sign) {

	}

	private fun tryEditFilter(player: Player, sign: Sign) { // State TBD

	}
}
