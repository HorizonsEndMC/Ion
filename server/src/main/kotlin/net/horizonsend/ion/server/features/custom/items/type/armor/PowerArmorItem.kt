package net.horizonsend.ion.server.features.custom.items.type.armor

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
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
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.setGliding
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword.Companion.addModifiers
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import java.util.function.Supplier
import kotlin.math.cos
import kotlin.math.sin

@Suppress("UnstableApiUsage")
class PowerArmorItem(
	key: IonRegistryKey<CustomItem, PowerArmorItem>,
	displayName: Component,
	itemModel: String,
	val slot: EquipmentSlot,
	val balancingsupplier: Supplier<PVPBalancingConfiguration.Armor.ArmorBalancing>
) : CustomItem(
	key,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
		.setCustomModel(itemModel)
		.setMaxStackSize(1)
		.addData(DataComponentTypes.UNBREAKABLE)
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
			.addModifiers(attributeList(balancingsupplier.get(), key.key, slot))
			.build()
		)
		.addFlag(ItemFlag.HIDE_UNBREAKABLE)
		.build()
	//.addFlag(ItemFlag.HIDE_ATTRIBUTES) //todo add this back after testing
) {
	val balancing get() = balancingsupplier.get()

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(POWER_STORAGE, PowerStorage(50000, 0, true))
		addComponent(MOD_MANAGER, ModManager(maxMods = 1))

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(40) {entity, itemStack, _, _ ->
			if(itemStack.itemMeta.attributeModifiers?.equals(attributeList(balancingsupplier.get(), key.key, slot)) == false) refreshModifiersFromBalancing(itemStack)
		})

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
		for (attribute in attributes.filterIsInstance<PotionEffectAttribute>()) {
			if (!attribute.requiredSlot.contains(slot)) continue
			attribute.addPotionEffect(entity, this, itemStack)
		}

		if (!getComponent(MOD_MANAGER).getModKeys(itemStack).contains(ItemModKeys.ROCKET_BOOSTING)) return
		if (entity !is Player) return
		if (entity.isGliding && !entity.world.hasFlag(WorldFlag.ARENA)) {
			powerManager.removePower(itemStack, this, 5)
		}
	}

	fun tickRocketBoots(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.FEET) return

		if (ActiveStarships.findByPilot(entity) != null && entity.inventory.itemInMainHand.type == Material.CLOCK) return

		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)
		if (!mods.contains(ItemModKeys.ROCKET_BOOSTING)) {
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
		val dir = entity.location.direction
		val strafeVel = entity.velocity.midpoint(dir.multiply(0.6))
		if(RocketBoostingMod.strafingMode[entity.uniqueId] == null && RocketBoostingMod.ascendingMode[entity.uniqueId] == null) entity.velocity = strafeVel
		else {
			val relativeUpAxis = when(entity.pitch) {
				90f -> Vector(-sin(entity.yaw * 0.017444) , 0.0, cos(entity.yaw * 0.017444)) // straight down
				-90f -> Vector( sin(entity.yaw * 0.017444) , 0.0,-cos(entity.yaw * 0.017444)) // straight up
				else -> Vector(-(dir.z), 0.0, (dir.x)).crossProduct(strafeVel) // anything else
			}
			val strafeRight = strafeVel.clone().crossProduct(relativeUpAxis)
			val finalVel = strafeVel.clone()
			when (RocketBoostingMod.strafingMode[entity.uniqueId]) {
				StrafingMode.LEFT -> entity.velocity = finalVel.rotateAroundAxis(relativeUpAxis, 0.26)
				StrafingMode.RIGHT -> entity.velocity = finalVel.rotateAroundAxis(relativeUpAxis, -0.26)
				else -> {}
			}
			when(RocketBoostingMod.ascendingMode[entity.uniqueId]) {
				AscendingMode.ASCENDING -> entity.velocity = finalVel.rotateAroundAxis(strafeRight, 0.2)
				AscendingMode.DESCENDING -> entity.velocity = finalVel.rotateAroundAxis(strafeRight, -0.2)
				else -> {}
			}
		}

		val footDir = entity.location.direction.normalize().multiply(-1)
			.rotateAroundX(randomDouble(0.20, 0.40))
			.rotateAroundY(randomDouble(0.20, 0.40))
			.rotateAroundZ(randomDouble(0.20, 0.40))
		entity.world.spawnParticle(Particle.SMOKE, entity.location, 0, footDir.x, footDir.y, footDir.z, 0.05)

		if (!entity.world.hasFlag(WorldFlag.ARENA) && entity.gameMode != GameMode.CREATIVE) {
			powerManager.removePower(itemStack, this, 5)
		}

		Tasks.sync {
			entity.world.playSound(entity.location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f)
		}
	}

	fun refreshModifiersFromBalancing(itemStack: ItemStack) {
		val attributeList = attributeList(balancingsupplier.get(), key.key, slot)
		val existing = itemStack.getData(DataComponentTypes.ATTRIBUTE_MODIFIERS)

		val builder = ItemAttributeModifiers.itemAttributes()

		if (existing != null) {
			for (modifier in existing.modifiers()) {
				val newModifier = attributeList[modifier.attribute()] ?: modifier.modifier()
				builder.addModifier(modifier.attribute(), newModifier)
			}
		}
		itemStack.setData(
			DataComponentTypes.ATTRIBUTE_MODIFIERS, builder.build(),)
		//.addFlag(ItemFlag.HIDE_ATTRIBUTES) //todo add this back after testing

		getComponent(POWER_STORAGE).setMaxPower(itemStack.customItem ?: return, itemStack, balancing.power)
	}
	companion object {
		fun attributeList(
			balancing: PVPBalancingConfiguration.Armor.ArmorBalancing,
			key: String,
			slot: EquipmentSlot
		) : MutableMap<Attribute, AttributeModifier> {
			return mutableMapOf(
				Attribute.MOVEMENT_SPEED to AttributeModifier(NamespacedKeys.key(key), balancing.speed , AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot.group),
				Attribute.SNEAKING_SPEED to AttributeModifier(NamespacedKeys.key(key), balancing.sneakSpeed , AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot.group),
				Attribute.SCALE to AttributeModifier(NamespacedKeys.key(key), balancing.scale , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.ENTITY_INTERACTION_RANGE to AttributeModifier(NamespacedKeys.key(key), balancing.entityReach , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.BLOCK_INTERACTION_RANGE to AttributeModifier(NamespacedKeys.key(key), balancing.blockReach , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.ARMOR to AttributeModifier(NamespacedKeys.key(key), balancing.armor , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.ARMOR_TOUGHNESS to AttributeModifier(NamespacedKeys.key(key), balancing.toughness, AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.KNOCKBACK_RESISTANCE to AttributeModifier(NamespacedKeys.key(key), balancing.knockBackResistance , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.STEP_HEIGHT to AttributeModifier(NamespacedKeys.key(key), balancing.stepHeight, AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.MAX_HEALTH to AttributeModifier(NamespacedKeys.key(key), balancing.maxHealth , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.JUMP_STRENGTH to AttributeModifier(NamespacedKeys.key(key), balancing.jumpStrength , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.FLYING_SPEED to AttributeModifier(NamespacedKeys.key(key), balancing.flyingSpeed , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.GRAVITY to AttributeModifier(NamespacedKeys.key(key), balancing.gravity , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.OXYGEN_BONUS to AttributeModifier(NamespacedKeys.key(key), balancing.oxygenBonus , AttributeModifier.Operation.ADD_NUMBER, slot.group),
				Attribute.WATER_MOVEMENT_EFFICIENCY to AttributeModifier(NamespacedKeys.key(key), balancing.waterMovementEfficiency , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			)
		}
	}
}

