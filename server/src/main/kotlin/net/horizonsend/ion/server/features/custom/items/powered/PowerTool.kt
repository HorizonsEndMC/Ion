package net.horizonsend.ion.server.features.custom.items.powered

import net.horizonsend.ion.server.features.custom.NewCustomItem
import net.horizonsend.ion.server.features.custom.items.components.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.components.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.components.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.components.ModManager
import net.horizonsend.ion.server.features.custom.items.components.Power
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

abstract class PowerTool(identifier: String, displayName: Component, private val modLimit: Int, private val basePowerCapacity: Int, val model: String) : NewCustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.DIAMOND_PICKAXE)
		.setCustomModel(model)
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.MODDED_ITEM, ModManager(modLimit))

		addComponent(CustomComponentTypes.POWERED_ITEM, Power(basePowerCapacity, 10, true))

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@PowerTool) { event, _, item ->
			tryOpenMenu(event, item)
		})
	}

	open fun tryOpenMenu(event: PlayerInteractEvent, itemStack: ItemStack) {
		if (!event.player.isSneaking) return
		val modManger = getComponent(CustomComponentTypes.MODDED_ITEM)
		modManger.openMenu(event.player, this, itemStack)
	}
}
