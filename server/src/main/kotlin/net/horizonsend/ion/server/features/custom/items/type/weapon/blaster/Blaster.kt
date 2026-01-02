package net.horizonsend.ion.server.features.custom.items.type.weapon.blaster

import io.papermc.paper.datacomponent.DataComponentTypes
import io.papermc.paper.datacomponent.item.Equippable
import io.papermc.paper.datacomponent.item.ItemAttributeModifiers
import io.papermc.paper.entity.LookAnchor
import net.horizonsend.ion.common.database.cache.nations.NationCache
import net.horizonsend.ion.common.database.schema.misc.SLPlayer
import net.horizonsend.ion.common.extensions.alert
import net.horizonsend.ion.common.extensions.userError
import net.horizonsend.ion.common.utils.miscellaneous.randomDouble
import net.horizonsend.ion.common.utils.text.template
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration
import net.horizonsend.ion.server.configuration.PVPBalancingConfiguration.BlasterWeapons.Balancing
import net.horizonsend.ion.server.core.registration.IonRegistryKey
import net.horizonsend.ion.server.core.registration.keys.CustomItemKeys
import net.horizonsend.ion.server.core.registration.keys.ItemModKeys
import net.horizonsend.ion.server.core.registration.registries.CustomItemRegistry.Companion.customItem
import net.horizonsend.ion.server.features.custom.items.CustomItem
import net.horizonsend.ion.server.features.custom.items.component.AmmunitionStorage
import net.horizonsend.ion.server.features.custom.items.component.CustomComponentTypes
import net.horizonsend.ion.server.features.custom.items.component.CustomItemComponentManager
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.leftClickListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.playerSwapHandsListener
import net.horizonsend.ion.server.features.custom.items.component.Listener.Companion.rightClickListener
import net.horizonsend.ion.server.features.custom.items.component.MagazineType
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergyGreatSword
import net.horizonsend.ion.server.features.custom.items.type.weapon.sword.EnergySword
import net.horizonsend.ion.server.features.custom.items.util.ItemFactory
import net.horizonsend.ion.server.features.world.IonWorld.Companion.hasFlag
import net.horizonsend.ion.server.features.world.IonWorld.Companion.ion
import net.horizonsend.ion.server.features.world.WorldFlag
import net.horizonsend.ion.server.miscellaneous.registrations.persistence.NamespacedKeys
import net.horizonsend.ion.server.miscellaneous.utils.Tasks
import net.horizonsend.ion.server.miscellaneous.utils.coordinates.alongVector
import net.horizonsend.ion.server.miscellaneous.utils.setModel
import net.horizonsend.ion.server.miscellaneous.utils.updateData
import net.kyori.adventure.audience.Audience
import net.kyori.adventure.key.Key.key
import net.kyori.adventure.sound.Sound.Source.PLAYER
import net.kyori.adventure.sound.Sound.sound
import net.kyori.adventure.text.Component
import net.kyori.adventure.text.Component.text
import net.kyori.adventure.text.format.NamedTextColor.RED
import net.minecraft.network.protocol.game.ClientboundSetPlayerInventoryPacket
import org.bukkit.Color
import org.bukkit.Color.fromRGB
import org.bukkit.Particle.DUST
import org.bukkit.Particle.DustOptions
import org.bukkit.attribute.Attribute
import org.bukkit.attribute.AttributeModifier
import org.bukkit.craftbukkit.entity.CraftPlayer
import org.bukkit.craftbukkit.inventory.CraftItemStack
import org.bukkit.entity.Flying
import org.bukkit.entity.LivingEntity
import org.bukkit.entity.Player
import org.bukkit.inventory.EquipmentSlot
import org.bukkit.inventory.EquipmentSlotGroup
import org.bukkit.inventory.InventoryHolder
import org.bukkit.inventory.ItemStack
import org.bukkit.potion.PotionEffect
import org.bukkit.potion.PotionEffectType.SLOWNESS
import org.bukkit.util.Vector
import java.util.function.Supplier

@Suppress("UnstableApiUsage")
open class Blaster<T : Balancing>(
	key: IonRegistryKey<CustomItem, out CustomItem>,
	displayName: Component,
	itemFactory: ItemFactory,
	private val balancingSupplier: Supplier<T>
) : CustomItem(
	key,
	displayName,
	itemFactory,
) {
	val balancing get() = balancingSupplier.get()

	val ammoComponent = AmmunitionStorage(balancingSupplier, balancing.consumesAmmo)
	val magazineComponent = MagazineType(balancingSupplier, CustomItemKeys[balancing.magazineIdentifier] ?: error("No custom item type ${balancing.magazineIdentifier}"))

	val model = itemFactory.customModel ?: ""
	override fun decorateItemStack(base: ItemStack) {
		// Clear base item attributes
		base.updateData(DataComponentTypes.ATTRIBUTE_MODIFIERS, ItemAttributeModifiers.itemAttributes().build())
		ammoComponent.setAmmo(base, this, balancing.capacity)
	}

	override val customComponents: CustomItemComponentManager = CustomItemComponentManager(serializationManager).apply {
		addComponent(CustomComponentTypes.AMMUNITION_STORAGE, ammoComponent)
		if (balancing.consumesAmmo) addComponent(CustomComponentTypes.MAGAZINE_TYPE, magazineComponent)

		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, rightClickListener(this@Blaster) { event, _, item ->
			val livingEntity = event.player
			var primaryCount = 0
			var secondaryCount = 0
			var tertiaryCount = 0
			var fattyBelt = 1
			var hasSword = false
			val inventory = (livingEntity as? InventoryHolder)?.inventory ?: return@rightClickListener
			for (i in inventory.contents){
				val customItem = i?.customItem ?: continue
				if (customItem is EnergySword || customItem is EnergyGreatSword) hasSword = true
				if (customItem is Blaster<*>){
					if (customItem.ammoComponent.getAmmo(i)==0) continue
					when(customItem.balancingSupplier.get().type){
						PVPBalancingConfiguration.WeaponTypeEnum.PRIMARY -> primaryCount++
						PVPBalancingConfiguration.WeaponTypeEnum.SECONDARY -> secondaryCount++
						PVPBalancingConfiguration.WeaponTypeEnum.TERTIARY -> tertiaryCount++
						else -> {}
					}
				}
				else continue
			}
			for (item in event.player.inventory.armorContents) {
				val customItem = item?.customItem ?: continue

				if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
				val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
				if (!mods.contains(ItemModKeys.EXTENSION_BELT)) continue
				fattyBelt = 2
			}
			if (primaryCount > fattyBelt){
				livingEntity.userError("Over Primary weapon limit, limit is $fattyBelt but you have $primaryCount weapons ")
				return@rightClickListener
			}
			else if (secondaryCount > 1){
				livingEntity.userError("Over Secondary weapon limit, limit is 1 but you have $secondaryCount weapons")
				return@rightClickListener
			}
			else if (tertiaryCount > 1){
				livingEntity.userError("Over Tertiary weapon limit, limit is 1 but you have $tertiaryCount weapons")
				return@rightClickListener
			}
			else if (hasSword){
				livingEntity.userError("You cannot wield energy swords and blasters at the same time!")
				return@rightClickListener
			}
			else{
				fire(event.player, item)
			}
		})
		addComponent(CustomComponentTypes.LISTENER_PLAYER_SWAP_HANDS, playerSwapHandsListener(this@Blaster) { event, _, item -> reload(event.player, item) })
		addComponent(CustomComponentTypes.LISTENER_PLAYER_INTERACT, leftClickListener(this@Blaster) { event, _, item ->
			if (balancing.shouldHaveCameraOverlay){
				if (!zoomIn(item, event.player)) zoomOut(item)
			}
		})
	}

	open fun fire(shooter: LivingEntity, blasterItem: ItemStack) {
		if (shooter is Player) {
			if (shooter.hasCooldown(blasterItem.type)) return // Cooldown

			if (!checkAndDecrementAmmo(blasterItem, shooter)) return reload(shooter, blasterItem)
		}

		val soundOrigin = shooter.location

		// Shell sound
		/*
		var relativeBlock = livingEntity.location.block.getRelative(DOWN)
		val maxDistance = 4 // Add 1 to this value for the actualStyle distance

		for (i in 0..maxDistance) {
			if (!relativeBlock.isSolid) {
				relativeBlock = relativeBlock.getRelative(DOWN)
				continue
			}

			Tasks.syncDelay(randomInt(5, 10).toLong()) {
				soundOrigin.world.playSound(soundOrigin, soundShell, PLAYERS, 0.5f, 1.0f)
			}
			break
		}
		*/

		// Shoot sound
		soundOrigin.world.players.forEach { player ->
			var distanceFactor = balancing.soundRange
			var volumeFactor = 1.0
			var pitchFactor = 1.0

			// No sounds in space (somewhat)
			if (shooter.world.ion.hasFlag(WorldFlag.SPACE_WORLD)) {
				distanceFactor *= 0.5
				volumeFactor *= 0.25
				pitchFactor *= 0.5
			}

			// Sound is unmodified if players within 0.5*range distance of shooter
			// Modify sound until fully inaudible at 2.0*range distance of shooter
			if (player.location.distance(soundOrigin) >= distanceFactor * 0.5 && player.location.distance(soundOrigin) < distanceFactor * 2) {
				volumeFactor *= (-1.0 / (2.0 * distanceFactor)) * player.location.distance(soundOrigin) + 1.25
				pitchFactor *= (-1.0 / (3.0 * distanceFactor)) * player.location.distance(soundOrigin) + 1.165
			}

			if (player.location.distance(soundOrigin) < distanceFactor * 2) {
				val modified = balancing.soundFire.copy(
					volume = volumeFactor.toFloat(),
					pitch = pitchFactor.toFloat()
				)

				player.playSound(modified.sound, soundOrigin.x, shooter.y, soundOrigin.z)
			}
		}

		fireProjectiles(shooter)
		//recoil(shooter)
	}

	open fun fireProjectiles(livingEntity: LivingEntity) {
		val location = livingEntity.eyeLocation.clone()
		val slowness = PotionEffect(SLOWNESS, 10, 1)
		livingEntity.addPotionEffect(slowness)
		location.y -= 0.125

		if (balancing.shotDeviation > 0) {
			val offsetX = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetY = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)
			val offsetZ = randomDouble(-1 * balancing.shotDeviation, balancing.shotDeviation)

			location.direction = location.direction.clone().add(Vector(offsetX, offsetY, offsetZ)).normalize()
		}

		location.add(location.direction.clone().multiply(0.125))

		RayTracedParticleProjectile(
			location,
			livingEntity,
			balancing,
			DUST,
			balancing.explosiveShot,
			DustOptions(
				getParticleColor(livingEntity),
				balancing.particleSize
			),
			balancing.soundWhizz,
		).fire()
	}

	/**
	 * Returns whether the reload was successful
	 **/
	private fun checkAndDecrementAmmo(itemStack: ItemStack, livingEntity: LivingEntity): Boolean {
		val ammo = ammoComponent.getAmmo(itemStack)
		if (ammo == 0) {
			livingEntity.playSound(sound(key("horizonsend:blaster.dry_shoot"), PLAYER, 1.0f, 1.0f))
			return false
		}

		ammoComponent.setAmmo(itemStack, this, ammo - 1)

		(livingEntity as? Player)?.setCooldown(itemStack.type, (balancing.timeBetweenShots - 1).coerceAtLeast(0))
		sendActionBarAmmo(livingEntity, ammo - 1)

		return true
	}

	fun reload(livingEntity: LivingEntity, blasterItem: ItemStack) {
		var speedyReload = false
		if (livingEntity !is Player) return // Player Only
		if (livingEntity.hasCooldown(blasterItem.type)) return // Cooldown

		val originalAmmo = ammoComponent.getAmmo(blasterItem)

		var ammo = originalAmmo
		if (ammo == balancing.capacity) return

		if (balancing.consumesAmmo) {
			for (magazineItem in livingEntity.inventory.filterNotNull()) {
				if (ammo >= balancing.capacity) break // Check if blaster magazine is full

				val magazineCustomItem = magazineItem.customItem ?: continue // To get magazine properties
				if (magazineCustomItem !is Magazine) continue // Just to smart cast

				if (magazineCustomItem.identifier != balancing.magazineIdentifier) continue // Only correct magazine

				val magazineAmmo = magazineCustomItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).getAmmo(magazineItem)
				val amountToTake = (balancing.capacity - ammo).coerceAtMost(magazineAmmo)

				magazineCustomItem.getComponent(CustomComponentTypes.AMMUNITION_STORAGE).setAmmo(magazineItem, magazineCustomItem, magazineAmmo - amountToTake)

				ammo += amountToTake
			}
		}

		if (livingEntity.world.hasFlag(WorldFlag.ARENA) || !balancing.consumesAmmo) {
			ammo = balancing.capacity
		}

		if (ammo - originalAmmo == 0) {
			livingEntity.playSound(sound(key("minecraft:item.bundle.drop_contents"), PLAYER, 5f, 2.00f))
			livingEntity.alert("Out of ammo!")
			return
		}

		for (item in (livingEntity).inventory.armorContents) {
			val customItem = item?.customItem ?: continue

			if (!customItem.hasComponent(CustomComponentTypes.MOD_MANAGER)) continue
			val mods = customItem.getComponent(CustomComponentTypes.MOD_MANAGER).getModKeys(item)
			if (!mods.contains(ItemModKeys.COGNITION_BOOSTING)) continue
			speedyReload = true
		}

		if (speedyReload) livingEntity.setCooldown(blasterItem.type, this.balancing.reload/2)
		else livingEntity.setCooldown(blasterItem.type, this.balancing.reload)

		ammoComponent.setAmmo(blasterItem, this, ammo)

		if (ammo <= 0) livingEntity.playSound(sound(key("minecraft:block.iron_door.open"), PLAYER, 5f, 2.00f))
		sendActionBarAmmo(livingEntity, ammo)

		// Start reload
		livingEntity.world.playSound(balancing.soundReloadStart.sound, livingEntity)

		// Finish reload
		Tasks.syncDelay(this.balancing.reload.toLong()) {
			livingEntity.world.playSound(balancing.soundReloadFinish.sound, livingEntity)
		}
	}

	private fun getParticleColor(entity: LivingEntity): Color {
		if (entity !is Player) return Color.RED // Not Player
		SLPlayer[entity.uniqueId]?.nation?.let { return fromRGB(NationCache[it].color) } // Nation
		return Color.RED // Not Player
	}

	fun sendActionBarAmmo(audience: Audience, count: Int) {
		audience.sendActionBar(template(text("Ammo: {0} / {1}", RED), count.coerceIn(0, balancing.capacity), balancing.capacity))
	}

	fun recoil(livingEntity: LivingEntity){

		for (iteration in 1..balancing.packetsPerShot) {
			if (livingEntity is Flying) return

			Tasks.asyncDelay(iteration.toLong()) {
				val location100InFront = livingEntity.eyeLocation.alongVector(livingEntity.eyeLocation.direction.multiply(100), 1).last()
				val x = location100InFront.x
				val y = location100InFront.y + balancing.recoil
				val z = location100InFront.z
				livingEntity.lookAt(x, y, z, LookAnchor.EYES)
			}
		}
	}

	fun zoomIn(item: ItemStack, playerHoldingIt: Player) : Boolean{
		//if we're zoomed in already then we do not need to continue
		if (item.getData(DataComponentTypes.EQUIPPABLE)?.cameraOverlay() == null) {
			item.setData(
				DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HAND)
					.cameraOverlay(key(balancing.cameraOverlay))
					.build()
			)
			item.editMeta {
				it.addAttributeModifier(
					Attribute.MOVEMENT_SPEED,
					AttributeModifier(
						NamespacedKeys.SCOPE_ZOOM,
						balancing.zoomEffect,
						AttributeModifier.Operation.ADD_SCALAR,
						EquipmentSlotGroup.MAINHAND
					)
				)
			}
			val nmsItem = CraftItemStack.asNMSCopy(item.clone().setModel(balancing.scopedInItemModel))
			val slot = playerHoldingIt.inventory.first(item)
			val itemModelPacket = ClientboundSetPlayerInventoryPacket(slot, nmsItem)
			(playerHoldingIt as CraftPlayer).handle.connection.send(itemModelPacket)
			item.setModel("empty")
			return  true
		}
		return false
	}

	fun zoomOut(item: ItemStack){
		item.setData(
			DataComponentTypes.EQUIPPABLE, Equippable.equippable(EquipmentSlot.HAND)
				.cameraOverlay(null)
				.build()
		)
		item.editMeta { it.removeAttributeModifier(Attribute.MOVEMENT_SPEED, AttributeModifier(NamespacedKeys.SCOPE_ZOOM, balancing.zoomEffect, AttributeModifier.Operation.ADD_SCALAR, EquipmentSlotGroup.MAINHAND)) }
		item.setModel(model)
	}
}

