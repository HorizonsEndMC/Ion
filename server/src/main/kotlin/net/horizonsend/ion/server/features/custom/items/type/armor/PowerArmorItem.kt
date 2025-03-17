package net.horizonsend.ion.server.features.custom.items.type.armor

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.Unbreakable
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.attribute.PotionEffectAttribute
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.MOD_MANAGER
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes.Companion.POWER_STORAGE
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.ModManager
import net.horizonsend.ion.server.features.custom.items.component.PowerStorage
import net.horizonsend.ion.server.features.custom.items.component.TickReceiverModule
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.ItemModRegistry
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.setGliding
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.registrations.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemStack

class PowerArmorItem(
	identifier: String,
	displayName: Component,
	itemModel: String,
	val slot: EquipmentSlot
) : CustomItem(
	identifier,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
		.setCustomModel(itemModel)
		.setMaxStackSize(1)
		.addData(DataComponentTypes.UNBREAKABLE, Unbreakable.unbreakable(false))
		.addData(DataComponentTypes.EQUIPPABLE, Equippable
			.equippable(slot)
			.damageOnHurt(false)
			.swappable(true)
			.equipSound(Key.key("minecraft", "item.armor.equip_netherite"))
			.assetId(NamespacedKeys.packKey("power_armor"))
			.build()
		)
		.addData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
			.itemAttributes()
			.addModifier(Attribute.ARMOR, AttributeModifier(NamespacedKeys.key(identifier), 2.0, AttributeModifier.Operation.ADD_NUMBER, slot.group))
//			.addModifier(Attribute.ARMOR_TOUGHNESS, AttributeModifier(NamespacedKeys.key(identifier), 2.0, AttributeModifier.Operation.ADD_NUMBER, slot.group))
			.build())
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(POWER_STORAGE, PowerStorage(50000, 0, true))
		addComponent(MOD_MANAGER, ModManager(maxMods = 1))

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(
			this@PowerArmorItem,
			additionalPreCheck = { it.player.isSneaking }
		) { event, _, item ->
			val modManger = getComponent(MOD_MANAGER)
			modManger.openMenu(event.player, this@PowerArmorItem, item)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(20) { entity, itemStack, _, _ ->
			tickPowerMods(entity, itemStack)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(1) { entity, itemStack, _, equipmentSlot ->
			tickRocketBoots(entity, itemStack, equipmentSlot)
		})
	}

	fun tickPowerMods(entity: LivingEntity, itemStack: ItemStack) {
		val powerManager = getComponent(POWER_STORAGE)
		val power = powerManager.getPower(itemStack)
		if (power <= 0) return

		val attributes = getAttributes(itemStack)
		for (attribute in attributes.filterIsInstance<PotionEffectAttribute>()) attribute.addPotionEffect(entity, this, itemStack)

		if (!getComponent(MOD_MANAGER).getMods(itemStack).contains(ItemModRegistry.ROCKET_BOOSTING)) return
		if (entity !is Player) return
		if (entity.isGliding && !entity.world.hasFlag(WorldFlag.ARENA)) {
			powerManager.removePower(itemStack, this, 5)
		}
	}

	fun tickRocketBoots(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.FEET) return

		if (ActiveStarships.findByPilot(entity) != null && entity.inventory.itemInMainHand.type == Material.CLOCK) return

		val mods = getComponent(MOD_MANAGER).getMods(itemStack)
		if (!mods.contains(ItemModRegistry.ROCKET_BOOSTING)) {
			return setGliding(entity, false)
		}

		val powerManager = getComponent(POWER_STORAGE)
		if (powerManager.getPower(itemStack) <= 0) {
			return setGliding(entity, false)
		}

		if ((glideDisabledPlayers[entity.uniqueId] ?: 0) > System.currentTimeMillis()) return setGliding(entity, false)
		glideDisabledPlayers[entity.uniqueId]?.let { glideDisabledPlayers.remove(entity.uniqueId) } // remove if not disabled

		@Suppress("DEPRECATION") // Any other method would cause weirdness not allow low flight
		// RocketBoostingMod sets glidingPlayers only on the ToggleSneakEvent (in PowerArmorListener)
		if (entity.isOnGround || !entity.isSneaking || !RocketBoostingMod.glidingPlayers.contains(entity.uniqueId)) {
			setGliding(entity, false)
			return
		}

		entity.isGliding = true
		entity.velocity = entity.velocity.midpoint(entity.location.direction.multiply(0.6))
		entity.world.spawnParticle(Particle.SMOKE, entity.location, 5)

		if (!entity.world.hasFlag(WorldFlag.ARENA)) {
			powerManager.removePower(itemStack, this, 5)
		}

		Tasks.sync {
			entity.world.playSound(entity.location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f)
		}
	}
}
