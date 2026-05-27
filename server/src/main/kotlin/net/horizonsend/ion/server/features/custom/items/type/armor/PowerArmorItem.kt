package net.horizonsend.ion.server.features.custom.items.type.armor

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.datacomponent.item.ItemEnchantments
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
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
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.ArmorLockMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.ArmorLockMod.forceDisableArmorLock
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.GravityFieldMod.forceDisableGravityBoots
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.GravityFieldMod.getGravityEnabled
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.HoverMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.HoverMod.forceDisableHoverBoots
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.HoverMod.getHoverEnabled
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.glideDisabledPlayers
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.RocketBoostingMod.setGliding
import net.horizonsend.ion.server.features.custom.items.type.tool.mods.armor.SwiftSneakMod.setSneakSpeed
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.player.CombatTimer
import net.horizonsend.ion.server.features.starship.active.ActiveStarships
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.helixAroundVector
import net.kyori.adventure.key.Key
import net.kyori.adventure.text.Component
import org.bukkit.Color
import org.bukkit.GameMode
import org.bukkit.Material
import org.bukkit.Particle
import org.bukkit.Sound
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.enchantments.Enchantment.PROTECTION
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.ItemFlag
import org.bukkit.inventory.ItemStack
import org.bukkit.util.Vector
import kotlin.math.cos
import kotlin.math.sin

@Suppress("UnstableApiUsage")
class PowerArmorItem(
	key: IonRegistryKey<CustomItem, PowerArmorItem>,
	displayName: Component,
	itemModel: String,
	wornModel: String,
	val slot: EquipmentSlot,
	val balancing: PVPBalancingConfiguration.Armor.AttributeHolder
) : CustomItem(
	key,
	displayName,
	ItemFactory
		.builder()
		.setMaterial(Material.WARPED_FUNGUS_ON_A_STICK)
		.setCustomModel(itemModel)
		.setMaxStackSize(1)
		.addData(DataComponentTypes.UNBREAKABLE)
		.addModifier { item ->
			item.editMeta { meta ->
				meta.addItemFlags(org.bukkit.inventory.ItemFlag.HIDE_UNBREAKABLE)
			}
		}
		.addData(DataComponentTypes.EQUIPPABLE, Equippable
			.equippable(slot)
			.damageOnHurt(false)
			.swappable(true)
			.equipSound(Key.key("minecraft", "item.armor.equip_netherite"))
			.assetId(NamespacedKeys.packKey(wornModel))
			.build()
		)
		.addData(DataComponentTypes.ENCHANTMENTS, ItemEnchantments.itemEnchantments(mapOf(PROTECTION to 4)))
		.addData(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, false)
		.addData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers
			.itemAttributes()
//			.addModifier(Attribute.ARMOR, AttributeModifier(NamespacedKeys.key(key.key), 2.0, AttributeModifier.Operation.ADD_NUMBER, slot.group))
//			.addModifier(Attribute.ARMOR_TOUGHNESS, AttributeModifier(NamespacedKeys.key(key.key), 2.0, AttributeModifier.Operation.ADD_NUMBER, slot.group))
			.build())
		.build()
) {
	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(POWER_STORAGE, PowerStorage(50000, 0, true))
		addComponent(MOD_MANAGER, ModManager(maxMods = balancing.maxPrimaryModules))

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

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(1) { entity, itemStack, _, equipmentSlot ->
			tickSwiftSneak(entity, itemStack, equipmentSlot)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(20) { entity, itemStack, _, equipmentSlot ->
			tickGravityBoots(entity, itemStack, equipmentSlot)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(1) { entity, itemStack, _, equipmentSlot ->
			tickHoverBoots(entity, itemStack, equipmentSlot)
		})

		addComponent(CustomComponentTypes.TICK_RECIEVER, TickReceiverModule(1) { entity, itemStack, _, equipmentSlot ->
			tickArmorLock(entity, itemStack, equipmentSlot)
		})
	}

	override fun decorateItemStack(base: ItemStack) {
		base.editMeta { itemMeta ->
			attributeList().forEach {
				itemMeta.addAttributeModifier(it.key, it.value)
			}
			itemMeta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES)
		}

	}

	fun attributeList() : MutableMap<Attribute, AttributeModifier> {
		return mutableMapOf(
			Attribute.MOVEMENT_SPEED to AttributeModifier(NamespacedKeys.key(identifier), balancing.speed , AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot.group),
			Attribute.SNEAKING_SPEED to AttributeModifier(NamespacedKeys.key(identifier), balancing.sneakSpeed , AttributeModifier.Operation.MULTIPLY_SCALAR_1, slot.group),
			Attribute.SCALE to AttributeModifier(NamespacedKeys.key(identifier), balancing.scale , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.ENTITY_INTERACTION_RANGE to AttributeModifier(NamespacedKeys.key(identifier), balancing.entityReach , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.BLOCK_INTERACTION_RANGE to AttributeModifier(NamespacedKeys.key(identifier), balancing.blockReach , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.ARMOR to AttributeModifier(NamespacedKeys.key(identifier), balancing.armor , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.ARMOR_TOUGHNESS to AttributeModifier(NamespacedKeys.key(identifier), balancing.toughness, AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.KNOCKBACK_RESISTANCE to AttributeModifier(NamespacedKeys.key(identifier), balancing.knockBackResistance , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.STEP_HEIGHT to AttributeModifier(NamespacedKeys.key(identifier), balancing.stepHeight, AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.MAX_HEALTH to AttributeModifier(NamespacedKeys.key(identifier), balancing.maxHealth , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.JUMP_STRENGTH to AttributeModifier(NamespacedKeys.key(identifier), balancing.jumpStrength , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.FLYING_SPEED to AttributeModifier(NamespacedKeys.key(identifier), balancing.flyingSpeed , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.GRAVITY to AttributeModifier(NamespacedKeys.key(identifier), balancing.gravity , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.OXYGEN_BONUS to AttributeModifier(NamespacedKeys.key(identifier), balancing.oxygenBonus , AttributeModifier.Operation.ADD_NUMBER, slot.group),
			Attribute.WATER_MOVEMENT_EFFICIENCY to AttributeModifier(NamespacedKeys.key(identifier), balancing.waterMovementEfficiency , AttributeModifier.Operation.ADD_NUMBER, slot.group),
		)
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
			powerManager.removePower(itemStack, this, 0)
		}
	}

	fun tickSwiftSneak(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.LEGS) return
		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)
		if (!mods.contains(ItemModKeys.SWIFT_SNEAK)) return
		if (!entity.isSneaking) return
		return setSneakSpeed(entity)
		}

	fun tickHoverBoots(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.FEET) return
		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)
		if (!mods.contains(ItemModKeys.HOVER)) {
			forceDisableHoverBoots(entity)
			return
		}
		if ((entity.world.hasFlag(WorldFlag.SPACE_WORLD)) && getHoverEnabled(entity)) {
			forceDisableHoverBoots(entity)
			return
		}
		if (HoverMod.hoverEnabledPlayers.contains(entity.uniqueId)) {
			entity.isFlying = true
			entity.allowFlight = true
			entity.flySpeed = 0.025f
			entity.velocity.y = 0.0
			val footDir = entity.location.direction.normalize().multiply(-1)
				.rotateAroundX(randomDouble(0.20, 0.40))
				.rotateAroundY(randomDouble(0.20, 0.40))
				.rotateAroundZ(randomDouble(0.20, 0.40))
			entity.world.spawnParticle(Particle.SMOKE, entity.location, 0, footDir.x, footDir.y, footDir.z, 0.05)
		}
	}

	fun tickArmorLock(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.LEGS) return
		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)
		if (!mods.contains(ItemModKeys.ARMOR_LOCK) && ArmorLockMod.armorLockEnabledPlayers.contains(entity.uniqueId)) {
			forceDisableArmorLock(entity)
			return
		}
		if (ArmorLockMod.armorLockEnabledPlayers.contains(entity.uniqueId)) {
			if (!entity.isSneaking) {
				forceDisableArmorLock(entity)
				return
			}
			entity.isInvulnerable = true
			entity.canPickupItems = false
			entity.velocity = Vector(0, 0, 0)
			val origin = entity.location
			helixAroundVector(origin, Vector(0.01, 1.0, 0.0), 0.8, 25, wavelength = 2 * Math.PI) {
				entity.world.spawnParticle(Particle.SOUL_FIRE_FLAME, it, 1, 0.0, 0.0, 0.0, 0.0, null, true)
			}
			if (System.nanoTime() - ArmorLockMod.armorLockEnabledPlayers[entity.uniqueId]!! >= ArmorLockMod.maxLockTime) {
				forceDisableArmorLock(entity)
			}
		}
		else return
	}


	fun tickGravityBoots(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.FEET) return
		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)
		if (!mods.contains(ItemModKeys.GRAVITY_FIELD)) {
			forceDisableGravityBoots(entity)
			return
		}
		if (!(entity.world.hasFlag(WorldFlag.SPACE_WORLD)) && !getGravityEnabled(entity.player!!)) {
			forceDisableGravityBoots(entity)
			return
		}
		if (getGravityEnabled(entity)) {
			val feetLocation = entity.location.clone().add(0.0, 0.1, 0.0)
			entity.world.spawnParticle(Particle.DUST, feetLocation, 5, 0.2, 0.05, 0.2, 0.0,
				Particle.DustOptions(Color.fromRGB(180, 0, 255), 1.0f), true)
		}
	}

	fun tickRocketBoots(entity: LivingEntity, itemStack: ItemStack, equipmentSlot: EquipmentSlot) {
		if (entity !is Player) return
		if (equipmentSlot != EquipmentSlot.FEET) return

		if (ActiveStarships.findByPilot(entity) != null && entity.inventory.itemInMainHand.type == Material.CLOCK) return

		val mods = getComponent(MOD_MANAGER).getModKeys(itemStack)

		if (CombatTimer.isPvpCombatTagged(entity)) {
			return setGliding(entity, false)
		}

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
			powerManager.removePower(itemStack, this, 0)
		}

		Tasks.sync {
			entity.world.playSound(entity.location, Sound.BLOCK_FIRE_AMBIENT, 1.0f, 2.0f)
		}
	}
}
