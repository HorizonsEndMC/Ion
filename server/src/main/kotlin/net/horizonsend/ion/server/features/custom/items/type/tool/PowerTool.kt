package net.horizonsend.ion.server.features.custom.items.type.tool

import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.ModManager
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.event.player.PlayerInteractEvent
import org.bukkit.inventory.ItemStack

abstract class PowerTool(identifier: String, displayName: Component, private val modLimit: Int, private val basePowerCapacity: Int, val model: String) : CustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.DIAMOND_PICKAXE)
		.setCustomModel(model)
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager().apply {
		addComponent(CustomComponentTypes.MOD_MANAGER, ModManager(modLimit))

		addComponent(CustomComponentTypes.POWER_STORAGE, PowerStorage(basePowerCapacity, 10, true))

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@PowerTool) { event, _, item ->
			tryOpenMenu(event, item)
		})
	}

	open fun tryOpenMenu(event: PlayerInteractEvent, itemStack: ItemStack) {
		if (!event.player.isSneaking) return
		val modManger = getComponent(CustomComponentTypes.MOD_MANAGER)
		modManger.openMenu(event.player, this, itemStack)
	}
}
