package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.damageEntityListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.damagedHoldingListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.prepareCraftListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material.SHIELD
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlotGroup

class EnergySword(type: String, color: TextColor) : CustomItem(
	"ENERGY_SWORD_${type.uppercase()}",
	displayName = ofChildren(Component.text(type.lowercase().replaceFirstChar { it.uppercase() }, color), Component.text(" Energy Sword", YELLOW)),
	baseItemFactory = ItemFactory.builder()
		.setMaterial(SHIELD)
		.setCustomModel("weapon/energy_sword/${type.lowercase()}_energy_sword")
		.addData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
			.itemAttributes()
			.addModifier(Attribute.ATTACK_DAMAGE, AttributeModifier(NamespacedKeys.key("energy_sword_damage"), 6.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
			.addModifier(Attribute.ATTACK_SPEED, AttributeModifier(NamespacedKeys.key("energy_sword_speed"), 1.8, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
			.build())
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@EnergySword) { event, _, item ->
			event.player.world.playSound(event.player.location, "energy_sword.swing", 1.0f, 1.0f)
			if (event.action != Action.LEFT_CLICK_BLOCK) return@leftClickListener
			val player = event.player

			if (player.gameMode != GameMode.CREATIVE) return@leftClickListener

			event.isCancelled = true
		})

		addComponent(CustomComponentTypes.LISTENER_PREPARE_CRAFT, prepareCraftListener(this@EnergySword) { event, customItem, item ->
			val permission = "gear.energysword." + customItem.identifier.lowercase().removePrefix("energy_sword_")
			if (!event.view.player.hasPermission(permission)) {
				event.view.player.userError("You can only craft yellow energy swords unless you donate for other colors!")
				event.inventory.result = null
			}
		})

		addComponent(CustomComponentTypes.LISTENER_DAMAGE_ENTITY, damageEntityListener(this@EnergySword) { event, customItem, item ->
			val damaged = event.entity
			damaged.world.playSound(Sound.sound(key("horizonsend:energy_sword.strike"), Sound.Source.PLAYER, 1.0f, 1.0f), damaged)
		})

		addComponent(CustomComponentTypes.LISTENER_DAMAGED_HOLDING, damagedHoldingListener(this@EnergySword) { event, customItem, item ->
			val damaged = event.entity
			if (damaged !is Player) return@damagedHoldingListener
			if (!damaged.isBlocking) return@damagedHoldingListener

			if (damaged.getCooldown(SHIELD) != 0) return@damagedHoldingListener

			val velocity = damaged.velocity
			Tasks.syncDelay(1) { damaged.velocity = velocity }

			event.damage = 0.0
			damaged.setCooldown(SHIELD, 15)
			damaged.setArrowsInBody(/* count = */ 0, /* fireEvent = */ false)

			damaged.world.playSound(Sound.sound(key("horizonsend:energy_sword.strike"), Sound.Source.PLAYER, 5.0f, 1.0f), damaged)
		})
	}
}
