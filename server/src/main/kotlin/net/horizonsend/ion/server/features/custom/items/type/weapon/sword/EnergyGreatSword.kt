package net.horizonsend.ion.server.features.custom.items.type.weapon.sword

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.text.BOLD
import net.horizonsend.ion.common.utils.text.ofChildren
import net.horizonsend.ion.server.configuration.ConfigurationFiles
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.BlockAmountComponent
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.damageEntityListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.damagedHoldingListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerSwapHandsListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.prepareCraftListener
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.custom.items.util.StoredValues
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.format.NamedTextColor.GOLD
import net.kyori.adventure.text.format.NamedTextColor.YELLOW
import net.kyori.adventure.text.format.TextColor
import org.bukkit.GameMode
import org.bukkit.Material.SHIELD
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.Player
import org.bukkit.event.block.Action
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.ItemStack

class EnergyGreatSword(key: IonRegistryKey<CustomItem, out CustomItem>, type: String, color: TextColor) : CustomItem(
	key = key,
	displayName = ofChildren(Component.text("★", GOLD, BOLD), Component.text(" Energy", YELLOW, BOLD), Component.text(" Greatsword", GOLD, BOLD)),
	baseItemFactory = ItemFactory.builder()
		.setMaterial(SHIELD)
		.setCustomModel("weapon/energy_sword/energy_greatsword")
		.addData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
			.itemAttributes()
			.addModifier(Attribute.ATTACK_DAMAGE, AttributeModifier(NamespacedKeys.key("energy_sword_damage"), 11.5, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
			.addModifier(Attribute.ATTACK_SPEED, AttributeModifier(NamespacedKeys.key("energy_sword_speed"), -2.4, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
			.addModifier(Attribute.ATTACK_KNOCKBACK, AttributeModifier(NamespacedKeys.key("energy_sword_knockback"), 2.0, AttributeModifier.Operation.ADD_NUMBER, EquipmentSlotGroup.MAINHAND))
			.build())
		.build()
) {
	val balancing = ConfigurationFiles.pvpBalancing().meleeWeapons::energyGreatswordBalancing.get()

	val blockComponent = BlockAmountComponent(balancing)

	override fun decorateItemStack(base: ItemStack) {
		blockComponent.setBlock(base, balancing.blockAmount, null)
		StoredValues.TIMELASTUSED.setAmount(base, (System.currentTimeMillis()/1000).toInt())
	}

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@EnergyGreatSword) { event, _, item ->
			event.player.world.playSound(event.player.location, "energy_sword.swing", 1.0f, 1.0f)
			if (event.action != Action.LEFT_CLICK_BLOCK) return@leftClickListener
			val player = event.player

			if (player.gameMode != GameMode.CREATIVE) return@leftClickListener

			event.isCancelled = true
		})

		addComponent(CustomComponentTypes.LISTENER_PREPARE_CRAFT, prepareCraftListener(this@EnergyGreatSword) { event, customItem, item ->
			val permission = "gear.energysword." + customItem.key.key.lowercase().removePrefix("energy_sword_")
			if (!event.view.player.hasPermission(permission)) {
				event.view.player.userError("You can only craft yellow energy swords unless you donate for other colors!")
				event.inventory.result = null
			}
		})

		addComponent(CustomComponentTypes.LISTENER_DAMAGE_ENTITY, damageEntityListener(this@EnergyGreatSword) { event, customItem, item ->
			val damaged = event.entity
			damaged.world.playSound(Sound.sound(key("horizonsend:energy_sword.strike"), Sound.Source.PLAYER, 1.0f, 1.0f), damaged)
		})

		addComponent(CustomComponentTypes.LISTENER_DAMAGED_HOLDING, damagedHoldingListener(this@EnergyGreatSword) { event, customItem, item ->
			val damaged = event.entity
			var critical = 1.0
			var blockDamage = 25
			val attacker = event.damager
			if (!attacker.isOnGround && attacker.velocity.y < 0 && (attacker as Player).isSprinting) { critical *= 1.5}
			if (damaged !is Player) return@damagedHoldingListener
			if (!damaged.isBlocking) return@damagedHoldingListener
			if (damaged.inventory.itemInMainHand == EnergySword || damaged.inventory.itemInMainHand == EnergyGreatSword) blockDamage = 150

			if (damaged.getCooldown(SHIELD) != 0) return@damagedHoldingListener

			if (damaged.isBlocking) {
				val block = blockComponent.getBlock(item, damaged)
				blockComponent.setBlock(item, (block-blockDamage), damaged)
			}

			damaged.world.playSound(Sound.sound(key("horizonsend:energy_sword.strike"), Sound.Source.PLAYER, 5.0f, 1.0f), damaged)
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_SWAP_HANDS, playerSwapHandsListener(this@EnergyGreatSword) { event, customItem, item ->
			//A 'parry' rebounds an incoming projectile perfectly to where the player is looking
			val player = event.player
			if ((player).hasCooldown(item))return@playerSwapHandsListener
			if (player.hasCooldown(item)) return@playerSwapHandsListener
			peopleToParryTime[player] = System.currentTimeMillis()
			player.setCooldown(item.type, 20) //Add a cooldown, so players don't spam it
			player.sendActionBar(Component.text("Tried to parry!", TextColor.color(0, 0, 255)))
		})
	}
	companion object {
		val peopleToParryTime: MutableMap<Player, Long> = mutableMapOf()
	}

}
